pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        AWS_ACCOUNT_ID = '975371763536'
        BACKEND_IMAGE = 'content-platform-backend'
        FRONTEND_IMAGE = 'content-platform-frontend'
        VERSION = 'latest'
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        BACKEND_ECR_REPO = "${ECR_REGISTRY}/${BACKEND_IMAGE}"
        FRONTEND_ECR_REPO = "${ECR_REGISTRY}/${FRONTEND_IMAGE}"
        BACKEND_SERVICE_NAME = 'backend'       // ✅ updated
        FRONTEND_SERVICE_NAME = 'frontend'     // ✅ updated
        BACKEND_TASK_FAMILY = 'content-platform-backend-task'
        FRONTEND_TASK_FAMILY = 'content-platform-frontend-task'
        DB_HOST = 'app-postgres.c9h8x8x8x8.us-east-1.rds.amazonaws.com'
        DB_USER = 'vivek'
        DB_PASSWORD = 'vivekbhaskar'
        ECR_REPO_FRONTEND = "frontend-repo" // Updated to match ecr.tf
        ECR_REPO_BACKEND = "backend-repo" // Updated to match ecr.tf
        ECS_CLUSTER = "app-cluster"
        RDS_ENDPOINT = "${aws_db_instance.postgres.endpoint}"
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
    }

    stage('Clone Repository') {
            steps {
                git url: 'https://github.com/vivekbhaskar746/ENOPSE2E.git', branch: 'main'
            }
        }

    stages {
        stage('Build Infrastructure') {
            steps {
                dir('infrastructure') {
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
                        sh '''
                        set -e
                        terraform init
                        terraform plan -out=tfplan
                        terraform apply -auto-approve tfplan
                        '''
                    }
                }
            }
        }


        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -Dmaven.test.skip=true'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'CI=false npm run build'
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
                            sh "docker build -t ${BACKEND_IMAGE}:${VERSION} ."
                        }
                    }
                }
                stage('Build Frontend Image') {
                    steps {
                        dir('frontend') {
                            sh "docker build -t ${FRONTEND_IMAGE}:${VERSION} ."
                        }
                    }
                }
            }
        }

        stage('Push to ECR') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
                    sh '''
                    set -e
                    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

                    for repo in $BACKEND_IMAGE $FRONTEND_IMAGE; do
                        if ! aws ecr describe-repositories --repository-names $repo > /dev/null 2>&1; then
                            aws ecr create-repository --repository-name $repo
                        fi
                    done

                    docker tag $BACKEND_IMAGE:$VERSION $BACKEND_ECR_REPO:$VERSION
                    docker push $BACKEND_ECR_REPO:$VERSION

                    docker tag $FRONTEND_IMAGE:$VERSION $FRONTEND_ECR_REPO:$VERSION
                    docker push $FRONTEND_ECR_REPO:$VERSION
                    '''
                }
            }
        }

        stage('Deploy to ECS') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials-id']]) {
                    sh '''
                    set -e

                    # Update backend task definition
                    BACKEND_TASK_DEF=$(aws ecs describe-task-definition --task-definition $BACKEND_TASK_FAMILY)
                    BACKEND_NEW_DEF=$(echo $BACKEND_TASK_DEF | jq --arg IMAGE "$BACKEND_ECR_REPO:$VERSION" '.taskDefinition | .containerDefinitions[0].image = $IMAGE | {family: .family, containerDefinitions: .containerDefinitions, executionRoleArn: .executionRoleArn, networkMode: .networkMode, requiresCompatibilities: .requiresCompatibilities, cpu: .cpu, memory: .memory}')
                    BACKEND_REVISION=$(aws ecs register-task-definition --cli-input-json "$BACKEND_NEW_DEF" | jq -r '.taskDefinition.taskDefinitionArn')
                    aws ecs update-service --cluster $ECS_CLUSTER --service $BACKEND_SERVICE_NAME --task-definition $BACKEND_REVISION

                    # Update frontend task definition
                    FRONTEND_TASK_DEF=$(aws ecs describe-task-definition --task-definition $FRONTEND_TASK_FAMILY)
                    FRONTEND_NEW_DEF=$(echo $FRONTEND_TASK_DEF | jq --arg IMAGE "$FRONTEND_ECR_REPO:$VERSION" '.taskDefinition | .containerDefinitions[0].image = $IMAGE | {family: .family, containerDefinitions: .containerDefinitions, executionRoleArn: .executionRoleArn, networkMode: .networkMode, requiresCompatibilities: .requiresCompatibilities, cpu: .cpu, memory: .memory}')
                    FRONTEND_REVISION=$(aws ecs register-task-definition --cli-input-json "$FRONTEND_NEW_DEF" | jq -r '.taskDefinition.taskDefinitionArn')
                    aws ecs update-service --cluster $ECS_CLUSTER --service $FRONTEND_SERVICE_NAME --task-definition $FRONTEND_REVISION
                    '''
                }
            }
        }

        stage('Verify RDS Connection') {
            steps {
                echo 'Verifying RDS connection...'
                sh '''
                mysql -h $RDS_ENDPOINT -u root -p"vivekbhaskar" -e "SHOW DATABASES;"
                '''
            }
        }
    }
}
