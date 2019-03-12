import jenkins.*
import jenkins.model.*
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Node;
import hudson.util.RemotingDiagnostics;
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.synopsys.arc.jenkins.plugins.ownership.nodes.NodeOwnerHelper;
import com.synopsys.arc.jenkins.plugins.ownership.jobs.JobOwnerHelper;
//import com.synopsys.arc.jenkins.plugins.ownership.folders.FolderOwnerHelper;
import org.jenkinsci.plugins.ownership.model.folders.*;
import com.synopsys.arc.jenkins.plugins.ownership.OwnershipDescription;
import com.synopsys.arc.jenkins.plugins.ownership.util.OwnershipDescriptionHelper;

//user = User.current()
def currentUser = jenkins.model.Jenkins.instance.getAuthentication().getName();
def List<String> artifacts = new ArrayList<String>()

def getNodesForOwner(user)
{
	def artifacts = []
	
	for (aSlave in hudson.model.Hudson.instance.slaves)
	{
		
		/*
		println('====================');
		println('Name: ' + aSlave.name);
		println('getLabelString: ' + aSlave.getLabelString());
		println('getNumExectutors: ' + aSlave.getNumExecutors());
		println('getRemoteFS: ' + aSlave.getRemoteFS());
		println('getMode: ' + aSlave.getMode());
		println('getRootPath: ' + aSlave.getRootPath());
		println('getDescriptor: ' + aSlave.getDescriptor());
		println('getComputer: ' + aSlave.getComputer());
		println('\tcomputer.isAcceptingTasks: ' + aSlave.getComputer().isAcceptingTasks());
		println('\tcomputer.isLaunchSupported: ' + aSlave.getComputer().isLaunchSupported());
		println('\tcomputer.getConnectTime: ' + aSlave.getComputer().getConnectTime());
		println('\tcomputer.getDemandStartMilliseconds: ' + aSlave.getComputer().getDemandStartMilliseconds());
		println('\tcomputer.isOffline: ' + aSlave.getComputer().isOffline());
		println('\tcomputer.countBusy: ' + aSlave.getComputer().countBusy());
		//if (aSlave.name == 'NAME OF NODE TO DELETE') {
		//  println('Shutting down node!!!!');
		//  aSlave.getComputer().setTemporarilyOffline(true,null);
		//  aSlave.getComputer().doDoDelete();
		//}
		println('\tcomputer.getLog: ' + aSlave.getComputer().getLog());
		println('\tcomputer.getBuilds: ' + aSlave.getComputer().getBuilds());
		*/
		
		OwnershipDescription descr = NodeOwnerHelper.Instance.getOwnershipDescription(aSlave);
		//println "Owner: "+OwnershipDescriptionHelper.getOwnerID(descr);
		owner = OwnershipDescriptionHelper.getOwnerID(descr)
		
		
		
		if( owner.equals(user) )
		{
			if( ! aSlave.getComputer().isOffline() )
			{
				artifacts.add( checkNode( aSlave ) )
				
			}
			artifacts.add(aSlave)
		}
	}
	
	return artifacts
}

def getFolderOwner(name = "haris")
{	
	def owner = ""
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f ->
			if( f.getParent() instanceof hudson.model.Hudson )
			{
				if( f.name.equals( name ) )
				{				
					OwnershipDescription descr = FolderOwnershipHelper.getInstance().getOwnershipDescription(f);
					//println "Owner: "+OwnershipDescriptionHelper.getOwnerID(descr);
					owner = OwnershipDescriptionHelper.getOwnerID(descr)
				}
			}
	}
	
	return owner
}

def checkNode( slave )
{
	
	print_ip = 'println InetAddress.localHost.hostAddress';
	print_hostname = 'println InetAddress.localHost.canonicalHostName';
	get_docker = 'def proc = "docker ps -a".execute(); proc.waitFor(); println proc.in.text'
	
	// here it is - the shell command, uname as example
	uname = 'def proc = "uname -a".execute(); proc.waitFor(); println proc.in.text';	
		println slave.name;
		//~ println RemotingDiagnostics.executeGroovy(print_ip, slave.getChannel());
		//~ println RemotingDiagnostics.executeGroovy(print_hostname, slave.getChannel());
		//~ println RemotingDiagnostics.executeGroovy(uname, slave.getChannel());
		
		return RemotingDiagnostics.executeGroovy(get_docker, slave.getChannel());
}

def owner = getFolderOwner()

def nodes = getNodesForOwner(owner)

println nodes
