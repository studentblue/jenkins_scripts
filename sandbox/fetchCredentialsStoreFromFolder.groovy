import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*

  
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each{ f ->
	println f.getName()
  	
  	
  	if( f.getParent() instanceof com.cloudbees.hudson.plugins.folder.Folder )
  		println "Sub Folder"
  	
  	
  	if( f.getParent() instanceof hudson.model.Hudson )
  		println "Top Level Folder"
  	
  
  	AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
	FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)

    if( property )
    {
  		println property.getStore()
  	}

}


import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.mig82.folders.properties.FolderProperties
  
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each{ f ->
	println f.getName()
  	
  	
  	if( f.getParent() instanceof com.cloudbees.hudson.plugins.folder.Folder )
  		println "Sub Folder"
  	
  	
  	if( f.getParent() instanceof hudson.model.Hudson )
  		println "Top Level Folder"
  	
  
  	AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
	FolderProperties property = folderAbs.getProperties().get(FolderProperties.class)
	
  	if( property )
  		println property.getProperties()
  	//println folderAbs.getProperties()
  	
    //if( property )
  	//	println property.getStore()

}


/*** BEGIN META {
 "name" : "add credentials to folder",
 "comment" : "Sample groovy script to add credentials to Jenkins's folder into global domain",
 "core": "1.609",
 "authors" : [
 { name : "Kuisathaverat" }
 ]
 } END META**/

import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*

String id = UUID.randomUUID().toString()
Credentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, "description:"+id, "user", "password")

Jenkins.instance.getAllItems(Folder.class)
    .findAll{it.name.equals('FolderName')}
    .each{
        AbstractFolder<?> folderAbs = AbstractFolder.class.cast(it)
        FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
        if(property != null){
            property.getStore().addCredentials(Domain.global(), c)
            println property.getCredentials().toString()
        }
}


import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.mig82.folders.properties.FolderProperties
  
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each{ f ->
	//println f.getName()
  	
  	
  	//if( f.getParent() instanceof com.cloudbees.hudson.plugins.folder.Folder )
  	//	println "Sub Folder"
  	
  	
  	if( f.getParent() instanceof hudson.model.Hudson )
  	{	
      	//println "Top Level Folder"
  	
  
  		AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
		FolderProperties property = folderAbs.getProperties().get(FolderProperties.class)
	
  		if( property )
  		{
  			temp = property.getProperties()
      	
      		println temp.getClass()
          	property.properties.each{ f1 -> println f1.key + " = " + f1.value  }
    	}
    }
  	//println folderAbs.getProperties()
  	
    //if( property )
  	//	println property.getStore()

}



import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
  
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each{ f ->
	
  	
  	
  	
  	
  	
  	if( f.getParent() instanceof hudson.model.Hudson )
  	{
  	
  
  		AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
		FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)

    	if( property )
    	{
  			store = property.getStore()
          
          	creds = property.getCredentials()
          
          	//println creds
          if( creds )
          {
          	creds.each{ f1 -> 
              println f1.getId()
			  
              println f1.getClass()
              
              if( f1 instanceof org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl )
              	println f1.getSecret()
            }
          }
          	
  		}
    }

}


import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
  
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each{ f ->
	
  	
  	
  	
  	
  	
  	if( f.getParent() instanceof hudson.model.Hudson )
  	{
  	
  
  		AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
		FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)

    	if( property )
    	{
  			store = property.getStore()
          
          	creds = property.getCredentials(StringCredentialsImpl.class)
          
          	//println creds
          if( creds )
          {
          	creds.each{ f1 -> 
              println f1.getId()
			  
              println f1.getClass()
              
              if( f1 instanceof org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl )
              	println f1.getSecret()
            }
          }
          	
  		}
    }

}
