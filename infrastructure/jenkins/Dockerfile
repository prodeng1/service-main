FROM jenkins/jenkins:lts

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl unzip \
    && curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-27.5.1.tgz -o /tmp/docker.tgz \
    && tar -xzf /tmp/docker.tgz -C /tmp \
    && mv /tmp/docker/docker /usr/local/bin/docker \
    && chmod +x /usr/local/bin/docker \
    && curl -fsSL https://services.gradle.org/distributions/gradle-8.12-bin.zip -o /tmp/gradle.zip \
    && mkdir -p /opt/gradle \
    && unzip -q /tmp/gradle.zip -d /opt/gradle \
    && ln -sf /opt/gradle/gradle-8.12/bin/gradle /usr/local/bin/gradle \
    && rm -rf /tmp/docker /tmp/docker.tgz \
    && rm -f /tmp/gradle.zip \
    && rm -rf /var/lib/apt/lists/*

COPY infrastructure/jenkins/init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/

USER jenkins
