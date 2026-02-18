pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
    timeout(time: 45, unit: 'MINUTES')
  }

  environment {
    // AWS
    AWS_REGION     = 'us-east-1'
    AWS_ACCOUNT_ID = '975371763536'
    AWS_DEFAULT_REGION = 'us-east-1'

    // ECS Cluster & Services (must match your Terraform)
    ECS_CLUSTER           = 'app-cluster'
    BACKEND_SERVICE_NAME  = 'backend'
    FRONTEND_SERVICE_NAME = 'frontend'
    BACKEND_TASK_FAMILY   = 'backend'
    FRONTEND_TASK_FAMILY  = 'frontend'

    // ECR repos (match Terraform-managed repos)
    BACKEND_IMAGE = 'backend-repo'
    FRONTEND_IMAGE = 'frontend-repo'
    ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
    BACKEND_ECR_REPO = "${ECR_REGISTRY}/${BACKEND_IMAGE}"
    FRONTEND_ECR_REPO = "${ECR_REGISTRY}/${FRONTEND_IMAGE}"

    // Image tag
    VERSION = 'latest'

    // DB placeholders — move to Jenkins credentials for production
    DB_USER = 'vivek'
    DB_PASSWORD = 'vivekbhaskar'

    // Terraform automation
    TF_IN_AUTOMATION = 'true'
    TF_INPUT = 'false'
    TF_LOCK_TIMEOUT = '5m'
  }

  stages {

    stage('Clone Repository') {
      steps {
        git url: 'https://github.com/vivekbhaskar746/ENOPSE2E.git', branch: 'master'
      }
    }

    stage('Tools Check (jq/python/mysql)') {
      steps {
        sh '''
          set -e
          terraform version || true
          aws --version || true
          docker --version || true
          if command -v jq >/dev/null 2>&1; then echo "jq: $(jq --version)"; else echo "jq not found; will use Python fallback."; fi
          if command -v python3 >/dev/null 2>&1; then echo "python3: $(python3 --version)"; else echo "python3 not found; please install jq or python3."; fi
          if command -v mysql >/dev/null 2>&1; then mysql --version; else echo "mysql client not found; RDS check will be skipped."; fi
        '''
      }
    }

    stage('Build Infrastructure') {
      steps {
        dir('infrastructure') {
          withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
            sh '''
              set -euo pipefail

              # Ensure workspace is writable
              chmod -R u+rwX .

              # Use remote backend (backend.tf must be configured for S3 + DynamoDB)

              rm -rf .terraform terraform.tfstate terraform.tfstate.backup
              terraform init -input=false -upgrade -reconfigure

              # Plan & Apply (fails if names collide or config invalid)
              terraform plan -input=false -lock-timeout=${TF_LOCK_TIMEOUT} -out=tfplan -no-color
              terraform apply -auto-approve tfplan

              rm -f tfplan
            '''
          }
        }
      }
    }

   stage('Fetch RDS Endpoint (Terraform output)') {
  steps {
    dir('infrastructure') {
      withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
        script {
          // Re-init backend in this workspace/agent with credentials
          sh '''
            set -euo pipefail
            terraform init -input=false -reconfigure
          '''
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
}
    stage('Build Backend (Maven)') {
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

    stage('Build Frontend (Node)') {
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

            # Ensure repos exist (create if missing)
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

            update_task_def_with_jq() {
              local family="$1"
              local image="$2"
              local td_json=$(mktemp)

              aws ecs describe-task-definition \
                --task-definition "$family" \
                --query 'taskDefinition' \
                --output json > "$td_json"

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
                  }' "$td_json")

              aws ecs register-task-definition \
                --cli-input-json "$new_def" \
                --query 'taskDefinition.taskDefinitionArn' \
                --output text
            }

            update_task_def_with_python() {
              local family="$1"
              local image="$2"

              aws ecs describe-task-definition \
                --task-definition "$family" \
                --query 'taskDefinition' \
                --output json > td.json

              python3 - <<PY > new_td.json
import json
td=json.load(open('td.json'))
td['containerDefinitions'][0]['image'] = '${BACKEND_ECR_REPO}:${VERSION}' if '${family}'=='${BACKEND_TASK_FAMILY}' else '${FRONTEND_ECR_REPO}:${VERSION}'
keys=['family','taskRoleArn','executionRoleArn','networkMode','containerDefinitions','volumes','placementConstraints','requiresCompatibilities','cpu','memory','pidMode','ipcMode','proxyConfiguration','inferenceAccelerators','runtimePlatform','ephemeralStorage']
print(json.dumps({k:td.get(k) for k in keys if k in td}))
PY

              aws ecs register-task-definition \
                --cli-input-json file://new_td.json \
                --query 'taskDefinition.taskDefinitionArn' \
                --output text
            }

            update_task_def() {
              local family="$1"
              local image="$2"
              if command -v jq >/dev/null 2>&1; then
                update_task_def_with_jq "$family" "$image"
              elif command -v python3 >/dev/null 2>&1; then
                update_task_def_with_python "$family" "$image"
              else
                echo "ERROR: Neither jq nor python3 available to mutate task definition JSON."
                exit 1
              fi
            }

            # Update backend TD and service
            BACKEND_REVISION=$(update_task_def "$BACKEND_TASK_FAMILY" "${BACKEND_ECR_REPO}:${VERSION}")
            aws ecs update-service --cluster "$ECS_CLUSTER" --service "$BACKEND_SERVICE_NAME" --task-definition "$BACKEND_REVISION" >/dev/null

            # Update frontend TD and service
            FRONTEND_REVISION=$(update_task_def "$FRONTEND_TASK_FAMILY" "${FRONTEND_ECR_REPO}:${VERSION}")
            aws ecs update-service --cluster "$ECS_CLUSTER" --service "$FRONTEND_SERVICE_NAME" --task-definition "$FRONTEND_REVISION" >/dev/null

            # Wait for services to stabilize (optional but recommended)
            aws ecs wait services-stable --cluster "$ECS_CLUSTER" --services "$BACKEND_SERVICE_NAME" "$FRONTEND_SERVICE_NAME"
            echo "ECS services are stable."
          '''
        }
      }
    }

    stage('Verify RDS Connection') {
      steps {
        echo "Verifying RDS connection on endpoint: ${env.RDS_ENDPOINT}"
        sh '''
          set -euo pipefail
          if command -v mysql >/dev/null 2>&1; then
            mysql -h "$RDS_ENDPOINT" -u "$DB_USER" -p"$DB_PASSWORD" -e "SHOW DATABASES;" || {
              echo "RDS connectivity check failed (MySQL)."
              exit 1
            }
          else
            echo "mysql client not present; skipping RDS connectivity check."
          fi
        '''
      }
    }
  }

  post {
    failure {
      // Attempt to salvage Terraform state if an errored.tfstate was written
      sh '''
        set +e
        if [ -f infrastructure/errored.tfstate ]; then
          cd infrastructure
          terraform state push errored.tfstate || true
        fi
      '''
    }
    always {
      echo "Pipeline finished (always)."
    }
  }
}