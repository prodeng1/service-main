import com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger
import jenkins.branch.OrganizationFolder
import jenkins.model.Jenkins
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator

def instance = Jenkins.get()
def folderName = System.getenv("GITHUB_ORG_FOLDER") ?: "prodeng1-org"
def repoOwner = System.getenv("GITHUB_OWNER") ?: "prodeng1"
def credentialsId = System.getenv("GITHUB_CREDENTIALS_ID") ?: ""

if (instance.getItem(folderName) == null) {
    def folder = instance.createProject(OrganizationFolder, folderName)
    def navigator = new GitHubSCMNavigator(repoOwner)

    if (credentialsId?.trim()) {
        navigator.setCredentialsId(credentialsId)
    }

    folder.getNavigators().add(navigator)
    folder.addTrigger(new PeriodicFolderTrigger("1m"))
    folder.save()
    println("Created Jenkins GitHub Organization Folder '${folderName}' for owner '${repoOwner}'")
}
