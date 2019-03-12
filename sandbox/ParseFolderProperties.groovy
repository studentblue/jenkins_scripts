import com.cloudbees.hudson.plugins.folder.*
import jenkins.model.*
import com.cloudbees.plugins.credentials.Credentials
import hudson.model.*

//Jenkins.instance.getAllItems(Folder.class).find{it.name.equals(folderName)}

List<String> artifacts = new ArrayList<String>()

//artifacts.add(this.binding.build.project.name)
//artifacts.add(System.getenv('JOB_NAME') )
//def job = this.binding.jenkinsProject

//artifacts.add(this.binding.build.project.name)
//artifacts.add(manager.build.project.getName())

//def myvariables = getBinding().getVariables()

/*
for (v in myvariables)
{
	artifacts.add( "${v} " + myvariables.get(v) )
}
*/

def build = Thread.currentThread().toString()
def regexp= ".+?/job/([^/]+)/.*"
def match = build  =~ regexp
def jobName = match[0][1]

artifacts.add( "Build: " + build + ", Job: " + jobName )

Jenkins.instance.getAllItems(Folder.class).each
{
	folderName = it.getName()
	parent = it.getParent()
	parentName = parent.getClass()
	artifacts.add(folderName + ": " + parentName )
}

return artifacts
