pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
    timeout(time: 45, unit: 'MINUTES')
  }

  environment {
    AWS_REGION          = 'us-east-1'
    AWS_ACCOUNT_ID      = '975371763536'

    // Images and tags
    BACKEND_IMAGE       = 'content-platform-backend'
    FRONTEND_IMAGE      = 'content-platform-frontend'
    VERSION             = 'latest'

    // ECR
    ECR_REGISTRY        = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
    BACKEND_ECR_REPO    = "${ECR_REGISTRY}/${BACKEND_IMAGE}"
    FRONTEND_ECR_REPO   = "${ECR_REGISTRY}/${FRONTEND_IMAGE}"

    // ECS
    BACKEND_SERVICE_NAME   = 'backend'
    FRONTEND_SERVICE_NAME  = 'frontend'
    BACKEND_TASK_FAMILY    = 'content-platform-backend-task'
    FRONTEND_TASK_FAMILY   = 'content-platform-frontend-task'
    ECS_CLUSTER            = 'app-cluster'

    // Legacy/GitOps ECR names you mentioned in comments (not used in push loop)
    ECR_REPO_FRONTEND = 'frontend-repo'
    ECR_REPO_BACKEND  = 'backend-repo'

    // DB placeholders (do NOT hardcode secrets in real pipelines)
    DB_USER = 'vivek'
    DB_PASSWORD = 'vivekbhaskar'
  }

  stages {

    stage('Clone Repository') {
      steps {
        git url: 'https://github.com/vivekbhaskar746/ENOPSE2E.git', branch: 'master'
      }
    }

    stage('Build Infrastructure') {
      steps {
        dir('infrastructure') {
          withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
            sh '''
              set -euo pipefail
              terraform init -input=false
              terraform plan -out=tfplan -input=false
              terraform apply -auto-approve tfplan
            '''
          }
        }
      }
    }

    stage('Fetch RDS Endpoint (Terraform output)') {
      steps {
        dir('infrastructure') {
          script {
            // Reads your TF output:
            //   output "db_host" { value = aws_db_instance.postgres.endpoint }
            def endpoint = sh(
              returnStdout: true,
              script: "terraform output -raw db_host"
            ).trim()

            if (!endpoint) {
              error "RDS endpoint (db_host) not found in Terraform outputs."
            }

            env.RDS_ENDPOINT = endpoint
            echo "Resolved RDS endpoint: ${env.RDS_ENDPOINT}"
          }
        }
      }
    }

    stage('Build Backend') {
      steps {
        dir('backend') {
          sh '''
            set -euo pipefail
            mvn -v
            mvn clean package -Dmaven.test.skip=true
          '''
        }
      }
    }

    stage('Build Frontend') {
      steps {
        dir('frontend') {
          sh '''
            set -euo pipefail
            node -v || true
            npm -v || true
            npm install
            CI=false npm run build
          '''
        }
      }
    }

    stage('Archive Artifacts') {
      steps {
        archiveArtifacts artifacts: '**/target/*.jar, **/build/**', fingerprint: true
      }
    }

    stage('Build Docker Images') {
      parallel {
        stage('Build Backend Image') {
          steps {
            dir('backend') {
              sh '''
                set -euo pipefail
                docker build -t ${BACKEND_IMAGE}:${VERSION} .
              '''
            }
          }
        }
        stage('Build Frontend Image') {
          steps {
            dir('frontend') {
              sh '''
                set -euo pipefail
                docker build -t ${FRONTEND_IMAGE}:${VERSION} .
              '''
            }
          }
        }
      }
    }

    stage('Push to ECR') {
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
          sh '''
            set -euo pipefail

            # Login to ECR
            aws ecr get-login-password --region "$AWS_REGION" | \
              docker login --username AWS --password-stdin "$ECR_REGISTRY"

            # Ensure both repositories exist (names must match the tags we push)
            for repo in "$BACKEND_IMAGE" "$FRONTEND_IMAGE"; do
              if ! aws ecr describe-repositories --repository-names "$repo" --region "$AWS_REGION" >/dev/null 2>&1; then
                aws ecr create-repository --repository-name "$repo" --region "$AWS_REGION"
              fi
            done

            # Tag & push backend
            docker tag "${BACKEND_IMAGE}:${VERSION}" "${BACKEND_ECR_REPO}:${VERSION}"
            docker push "${BACKEND_ECR_REPO}:${VERSION}"

            # Tag & push frontend
            docker tag "${FRONTEND_IMAGE}:${VERSION}" "${FRONTEND_ECR_REPO}:${VERSION}"
            docker push "${FRONTEND_ECR_REPO}:${VERSION}"
          '''
        }
      }
    }

    stage('Deploy to ECS') {
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
          sh '''
            set -euo pipefail

            # Helper to update a task def's first container image
            update_task_def() {
              local family="$1"
              local image="$2"
              local tmp_json=$(mktemp)

              aws ecs describe-task-definition \
                --task-definition "$family" \
                --query 'taskDefinition' \
                --output json > "$tmp_json"

              # Replace the image for the first container; for production, consider selecting by name
              local new_def=$(jq --arg IMAGE "$image" '
                  .containerDefinitions[0].image = $IMAGE
                  | {
                      family: .family,
                      taskRoleArn: .taskRoleArn,
                      executionRoleArn: .executionRoleArn,
                      networkMode: .networkMode,
                      containerDefinitions: .containerDefinitions,
                      volumes: .volumes,
                      placementConstraints: .placementConstraints,
                      requiresCompatibilities: .requiresCompatibilities,
                      cpu: .cpu,
                      memory: .memory,
                      pidMode: .pidMode,
                      ipcMode: .ipcMode,
                      proxyConfiguration: .proxyConfiguration,
                      inferenceAccelerators: .inferenceAccelerators,
                      runtimePlatform: .runtimePlatform,
                      ephemeralStorage: .ephemeralStorage
                    }' "$tmp_json")

              aws ecs register-task-definition \
                --cli-input-json "$new_def" \
                --query 'taskDefinition.taskDefinitionArn' \
                --output text
            }

            # Update backend
            BACKEND_REVISION=$(update_task_def "$BACKEND_TASK_FAMILY" "${BACKEND_ECR_REPO}:${VERSION}")
            aws ecs update-service --cluster "$ECS_CLUSTER" --service "$BACKEND_SERVICE_NAME" --task-definition "$BACKEND_REVISION"

            # Update frontend
            FRONTEND_REVISION=$(update_task_def "$FRONTEND_TASK_FAMILY" "${FRONTEND_ECR_REPO}:${VERSION}")
            aws ecs update-service --cluster "$ECS_CLUSTER" --service "$FRONTEND_SERVICE_NAME" --task-definition "$FRONTEND_REVISION"
          '''
        }
      }
    }

    stage('Verify RDS Connection') {
      steps {
        echo "Verifying RDS connection on endpoint: ${env.RDS_ENDPOINT}"
        sh '''
          set -euo pipefail
          # Example uses MySQL client; ensure your engine is MySQL.
          # If Postgres, switch to: psql "host=$RDS_ENDPOINT user=$DB_USER password=$DB_PASSWORD dbname=postgres" -c "\\l"
          mysql --version || true
          mysql -h "$RDS_ENDPOINT" -u "$DB_USER" -p"$DB_PASSWORD" -e "SHOW DATABASES;"
        '''
      }
    }
  }
}
