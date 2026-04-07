import hudson.model.JDK
import hudson.plugins.gradle.GradleInstallation
import jenkins.model.Jenkins

def instance = Jenkins.get()

def jdkDescriptor = instance.getDescriptorByType(JDK.DescriptorImpl)
def jdkInstallations = [new JDK("JDK21", "/opt/java/openjdk")] as JDK[]
jdkDescriptor.setInstallations(jdkInstallations)
jdkDescriptor.save()

def gradleDescriptor = instance.getDescriptorByType(GradleInstallation.DescriptorImpl)
def gradleInstallations = [new GradleInstallation("Gradle-8.12", "/opt/gradle/gradle-8.12", [])] as GradleInstallation[]
gradleDescriptor.setInstallations(gradleInstallations)
gradleDescriptor.save()

println("Configured Jenkins tools: JDK21 and Gradle-8.12")
