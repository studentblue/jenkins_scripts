import jenkins.*
import jenkins.model.*
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Node;
import com.synopsys.arc.jenkins.plugins.ownership.nodes.NodeOwnerHelper;
import com.synopsys.arc.jenkins.plugins.ownership.jobs.JobOwnerHelper;
import com.synopsys.arc.jenkins.plugins.ownership.OwnershipDescription;
import com.synopsys.arc.jenkins.plugins.ownership.util.OwnershipDescriptionHelper;

//user = User.current()
def currentUser = jenkins.model.Jenkins.instance.getAuthentication().getName();
def List<String> artifacts = new ArrayList<String>()

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
	
	
	
	if( owner.equals(currentUser) )
		artifacts.add(aSlave.name)
}

return artifacts
