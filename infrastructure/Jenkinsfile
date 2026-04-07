pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                checkout scm
                sh 'chmod +x gradlew && ./gradlew clean build'
            }
        }

        stage('Compute Image Version') {
            steps {
                script {
                    def latestTag = sh(
                        script: "git tag --list --sort=-v:refname | head -n 1",
                        returnStdout: true
                    ).trim()

                    if (!latestTag) {
                        env.IMAGE_VERSION = '0.1.0'
                    } else {
                        def parts = latestTag.tokenize('.')
                        if (parts.size() != 3 || !parts.every { it.isInteger() }) {
                            error("Latest git tag '${latestTag}' does not follow the expected MAJOR.MINOR.PATCH format.")
                        }

                        env.IMAGE_VERSION = "${parts[0]}.${parts[1].toInteger() + 1}.${parts[2]}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                withCredentials([string(credentialsId: 'docker_image_name', variable: 'IMAGE_NAME')]) {
                    sh 'docker build -t ${IMAGE_NAME}:${IMAGE_VERSION} .'
                }
            }
        }

        stage('Publish Docker Image') {
            steps {
                withCredentials([
                    string(credentialsId: 'docker_image_name', variable: 'IMAGE_NAME'),
                    usernamePassword(
                        credentialsId: 'docker_hub_credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login docker.io -u "$DOCKER_USERNAME" --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_VERSION}
                    '''
                }
            }
        }

        stage('Create Git Tag') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'github_credentials',
                    usernameVariable: 'GITHUB_USERNAME',
                    passwordVariable: 'GITHUB_TOKEN'
                )]) {
                    sh '''
                        git config user.name "Jenkins CI"
                        git config user.email "jenkins@local"
                        git tag ${IMAGE_VERSION}
                        git push https://${GITHUB_USERNAME}:${GITHUB_TOKEN}@github.com/prodeng1/service-main.git refs/tags/${IMAGE_VERSION}
                    '''
                }
            }
        }
    }
}
