import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.mig82.folders.properties.FolderProperties

import groovy.json.*
import org.boon.Boon

//"$schema":"http://json-schema.org/draft-04/schema",

def jsonEditorOptions = Boon.fromJson(/{
	disable_edit_json: true,
	disable_properties: true,
	no_additional_properties: true,
	disable_collapse: true,
	disable_array_add: true,
	disable_array_delete: true,
	disable_array_reorder: true,
	theme: "bootstrap3",
	keep_oneof_values : false,
	show_errors: always,
	template: "mustache",
	"schema":
	{
		type:	object,
		properties:
		{
			"DockerHub":
			{
				"type": "object",
				format: grid,
				"properties":
				{
					repo:	{type:string, "propertyOrder": 1},
					tag:	{type:string, "propertyOrder": 2}
				},
				additionalProperties: false
			},
			Namespace:
			{
				oneOf:
				[
					{
						title: existing namespace,
						type: object,
						format: grid,
						properties:
						{
							name: 
							{ 
								"propertyOrder": 1,
								oneOf:[]
							},
							teamFromNamespace:
							{ 
								type: boolean, default: true, title: team from namespace, format: checkbox, readOnly: true,
								"propertyOrder": 10
							}
							
						},
						"additionalItems": false
					},
					{
						title: new namespace,
						type: object,
						format: grid,
						properties:
						{
							newName: 
							{
								title: name,
								"propertyOrder": 1,
								oneOf:
								[
									{
										title: input, type:string, pattern: "^[a-z0-9]+[a-z0-9-_]*[a-z0-9]$"
										
									},
									{
										type: boolean, default: true, title: generate, format: checkbox, readOnly: true
									}
									
								]
							},
							description: 
							{
								title: namespace description,
								"propertyOrder": 2,
								oneOf:
								[
									{
										title: input, type:string
									},
									{
										type: boolean, default: true, title: generate, format: checkbox, readOnly: true
									}
								]
							},
							team: 
							{
								oneOf:
								[
									{
										title: existing team,
										oneOf:[]
									},
									{
										type: object,
										display: grid,
										title: new team,
										properties:
										{
											name: 
											{
												title: teamname, type:string,
												"propertyOrder": 3
											},
											new:
											{
												type: boolean, default: true, title: new team , format: checkbox, readOnly: true,
												"propertyOrder": 4
											},
											description: 
											{
												title: team description ( optional ), type:string,
												"propertyOrder": 5
											}
										}
									}
								]
							}
						},
						"additionalItems": false
					}							
				]
			},
			Repo:
			{
				type: object,
				format: grid,
				properties:
				{
					name:
					{
						title: "\/repo_name",
						oneOf:
						[
							{
							title: generate, type:boolean, readOnly: true, default: true, format: checkbox,
								"propertyOrder": 2
							},
							{
								title: input, type:string,
								"propertyOrder": 2
							}
						]
					},
					tag:
					{
						title: ":tag_name",
						oneOf:
						[
							{
								title: generate, type:boolean, readOnly: true, default: true, format: checkbox,
								"propertyOrder": 3
							},
							{
								title: "input",type:string,
								"propertyOrder": 3
							}
						]
					}
				}
				type:
				{
					title: Type,
					type: string,
					enum: [base-with-java, mysql-DB, postgres-DB]
				}
			}
		}
	},
	startval:
	{
		"DockerHub":
		{
			repo:	"",
			tag:	""
		},
		Namespace:
		{
			name: ns1,
			teamFromNamespace: true
		},
		Repo:
		{
			name: true,
			tag: true,
			type: base-with-java
		}
	}
}/);

def url = ""
def user = ""
def token = ""
def api = ""

def getCredentials(credId = "portus_api_token", repoId = "REPO_URL", userId = "PORTUS_USER")
{
	def credentials = [:]
	
	def tokenKey = "TOKEN"
	def repoUrlKey = "REPO_URL"
	def userIdKey = "REPO_USER_ID"
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f ->
			if( f.getParent() instanceof hudson.model.Hudson )
			{
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
				
				if( property )
				{
					creds = property.getCredentials(StringCredentialsImpl.class)
					
					//println creds
					
					if( creds )
					{
						creds.each
						{
							f1 ->
								if( f1.getId().trim().equals(credId) )
								{
									credentials.put(tokenKey, f1.getSecret())
									return true
								}
						}
					}
				}
			}
	}
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f ->
			if( f.getParent() instanceof hudson.model.Hudson )
			{	
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderProperties property = folderAbs.getProperties().get(FolderProperties.class)
				
				if( property )
				{
					property.properties.each
					{
						f1 -> 
							if( f1.key.trim().equals(repoId) )
							{
								credentials.put(repoUrlKey, f1.value)
							}
							
							if( f1.key.trim().equals(userId) )
							{
								credentials.put(userIdKey, f1.value)
							}
					}
				}
			}
	}
	
	url = credentials[repoUrlKey]
	user = credentials[userIdKey]
	token = credentials[tokenKey]
	
}

def portusApiGET(api)
{
	def url2 = url + api
	def get = new URL(url2).openConnection();
	
	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${user}:${token}")
	
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
		return response
	}
	else
		return []
}


def getTeamsFromData( teams , mode)
{
	def values = []
	def options = []
	
	for(team in teams)
	{
		//Values
		values.add(team.name)
		
		//description / option
		def desc = " Name: "+team.name
		if( team.description )
		{
			def sani = team.description.trim()
			if( sani.length() > 15 )
				desc += " ( "+sani.substring(0,14)+" ... ) "
			else
				desc += " ( "+sani+" )"
		}
		options.add(desc)
		
		if( mode.equals("V1") )
			break
	}
	
	if( mode.equals("V1") )
	{
		if( values )
			return values[0]
		else
			return ""
	}
	
	//~ options.add("Define own Team")
	//~ values.add("custom")
	
	if( mode.equals("V") )
		return values
	
	if( mode.equals("O") )
		return options
	
	return []	
}

def getNameSpacesFromData( namespaces , mode)
{
	def values = []
	def options = []
	
	for(namespace in namespaces)
	{
		//println namespace["global"]
		if( namespace.global == true)
			continue
		
		//Values
		values.add(namespace.name)
		
		//description / option
		def desc = " Name: "+namespace.name
		
		if( namespace.description )
		{
			def sani = namespace.description.trim()
			if( sani.length() > 30 )
				desc += " ( "+sani.substring(0,29)+" ... ) "
			else
				desc += " ( "+sani+" )"
		}
		
		desc += ", Visibility: "+namespace.visibility
		
		if( namespace.team && namespace.team["name"] )
		{
			desc += ", Team: "+namespace.team["name"]
		}
		options.add(desc)
		
		if( mode.equals("V1") )
			break
	}
	
	if( mode.equals("V1") )
	{
		if( values )
			return values[0]
		else
			return ""
	}
	
	//~ options.add("Define own NameSpace")
	//~ values.add("custom")
	
	if( mode.equals("V") )
		return values
	
	if( mode.equals("O") )
		return options
	
	return []	
}

getCredentials()
def namespaces = portusApiGET("/api/v1/namespaces?all=true")
def teams = portusApiGET("/api/v1/teams?all=true")

//~ jsonEditorOptions.schema.properties.Namespace.oneOf[0].properties.name.enum = getNameSpacesFromData( namespaces , "V")
//~ jsonEditorOptions.schema.properties.Namespace.oneOf[0].properties.name.options.enum_titles = getNameSpacesFromData( namespaces , "O")

//~ jsonEditorOptions.schema.properties.Namespace.oneOf[0].properties.name = []

jsonEditorOptions.schema.properties.Namespace.oneOf[0].properties.name.oneOf = []

def startNameSpace = false

namespaces.each
{
	namespace ->
  		if( namespace.global == true )
			return false
		
		if( ! startNameSpace )
			startNameSpace = namespace
		
		def props = [
						"id": ["type": "integer", "default": namespace.id, "readOnly": true],
						"name": ["type": "string", "default": namespace.name, "readOnly": true],
						"description": ["type": "string", "default": (namespace.description ? namespace.description : "") , "readOnly": true],
						"team": ["type": "string", "default": (namespace.team ? namespace.team.name : ""), "readOnly": true],
						"visibility": ["type": "string", "default": namespace.visibility, "readOnly": true]
					]

  		def temp = ["title": namespace.name, "type": "object", "format": "grid", "propertyOrder": 1, "properties": props ]
		jsonEditorOptions.schema.properties.Namespace.oneOf[0].properties.name.oneOf.add( temp )
}

jsonEditorOptions.startval.Namespace.name = 
[ 
	"id": startNameSpace.id, "name": startNameSpace.name, "description": (startNameSpace.description ? startNameSpace.description : ""), 
	"team": ( startNameSpace.team ? startNameSpace.team.name : ""), "visibility": startNameSpace.visibility
]

jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].oneOf = []

teams.each
{
	team -> 
			
		def props = [
						"id": ["type": "integer", "default": team.id, "readOnly": true],
						"name": ["type": "string", "default": team.name, "readOnly": true],
						"description": ["type": "string", "default": (team.description ? team.description : "") , "readOnly": true],						
						"hidden": ["type": "boolean", "default": team.hidden, "readOnly": true, "format": "checkbox"]
					]

  		def temp = ["title": team.name, "type": "object", "format": "grid", "propertyOrder": 1, "properties": props ]
		jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].oneOf.add( temp )
}

//~ jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].enum = getTeamsFromData( teams , "V")
//~ jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].options.enum_titles = getTeamsFromData( teams , "O")

//~ jsonEditorOptions.schema.properties.Portus.properties.namespace.oneOf[0].options.enum_titles = getNameSpacesFromData( namespaces , "O")
//~ jsonEditorOptions.schema.properties.Portus.properties.team.oneOf[1].options.enum_titles = getTeamsFromData( teams , "O")
//~ jsonEditorOptions.startval.Namespace.name = getNameSpacesFromData( namespaces , "V1")

return jsonEditorOptions

/*
test:
							{
								oneOf:
								[
									{
										title: ns1,
										type: object,
										format: grid,
										properties:
										{
											name: {type: string, readOnly: true, default: ns1},
											desc: {type: string, readOnly: true, default: namespace1},
											id: {type: number, readOnly: true, default: 1}
										}
									},
									{
										title: ns2,
										type: object,
										format: grid,
										properties:
										{
											name: {type: string, readOnly: true, default: ns2},
											desc: {type: string, readOnly: true, default: namespace2},
											id: {type: number, readOnly: true, default: 2}
										}
									}
								]
							},
*/
//~ type:string, enum:[n1, n2, n3], "options":{"enum_titles": [ns1, ns2, ns3]}, 
//~ "propertyOrder": 1
