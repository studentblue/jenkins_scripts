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

def jsonEditorOptions = Boon.fromJson(/{
	disable_edit_json: true,
	disable_properties: true,
	disable_collapse: true,
	theme: "bootstrap3",
	keep_oneof_values : false,
	show_errors: "interaction",	
	"schema":
	{
		type: "object",
		format: categories,
		properties:
		{
			NameSpace:
			{
				"propertyOrder": 2,
				type: object,
				oneOf:
				[
					{
						title: "New NameSpace",
						properties:
						{					
							name:
							{
								oneOf:
								[
									{
										
										title: input,
										type: string, default: "my-namespace",
										description: "New Namespace for this stack under which all images will be pushed"
									},
									{
										title: generate,
										type: boolean, readOnly:true, default: true, format: checkbox,
										description: "Generate New Namespace for this stack under which all images will be pushed"
									}
								]
							},
							new:
							{
								type: boolean, readOnly:true, default: true, format: checkbox, options:{hidden: true}
							},
							team:
							{
								title: team,
								oneOf:[]
							}
						},
						"additionalProperties": false,
						required:[name, new, team]
					},
					{
						title: "Existing Namespace",
						properties:
						{
							name:
							{
								type: string, enum:[],
								description: "Images will be pushed under this namespace"
							}
						},
						"additionalProperties": false,
						required:[name]
					}
				]
			},
			Compile:
			{
				type: object,
				"propertyOrder": 1,
				format: categories,
				properties:
				{
					image:
					{
						type: object,
						"propertyOrder": 1,
						oneOf:
						[
							{
								title: DockerHub,
								format:grid,
								properties:
								{
									namespace:
									{
										"propertyOrder": 1,
										title: namespace,
										type: string
									},
									repo:
									{
										"propertyOrder": 2,
										title: "\/repo",
										type: string
									},
									tag:
									{
										"propertyOrder": 3,
										title: ":tag",
										type: string
									},
									fromDockerHub:
									{
										"propertyOrder": 4,
										type: boolean, readOnly: true, format: checkbox, default: true,
										options:{hidden: true}
									}
								},
								"additionalProperties": false
							},
							{
								type: object,
								title: Cpsiot,
								format:grid,
								properties:
								{
									name:
									{
										type: string, enum:["test\/test:1", "test2\/test:122", "test3\/test:100"]
									},									
									fromCpsiot:
									{
										"propertyOrder": 5,
										type: boolean, readOnly: true, format: checkbox, default: true,
										options:{hidden: true}
									}
								},
								"additionalProperties": false
							}
						]
					},
					imageArgs:{type: string, default: "", "propertyOrder": 2},
					command:{type: string, default: mvn, "propertyOrder": 3},
					args: {type: string, default: "", "propertyOrder": 4}
				}
				
			},			
			Images:
			{
				type: array,
				"propertyOrder": 3,
				format: tabs-top,
				"title": "Images",
				items:
				{
					type: object,
					"headerTemplate": "{{ self.repo }}",
					"title": "Image",
					oneOf:
					[
						{
							"title": "DB",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								repo:{title: "\/repo", type: string, default: my-database, "propertyOrder": 3},
								database:{type: boolean, default: true, readOnly: true, format: checkbox, options:{ hidden: true}}
							},
							required:[repo,database],
							"additionalProperties": false
						},
						{
							"title": "SERVICE_REGISTRY",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								repo:{title: "\/repo", type: string, default: my-service-registry, "propertyOrder": 3},
								registry:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "serviceregistry_sql\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/serviceregistry_sql", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-serviceregistry-sql-4.1.2.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								}
							},
							required:
							[
								repo,registry, buildImage
							],
							"additionalProperties": false
						},
						{
							"title": "AUTHORIZATION",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								repo:{title: "\/repo", type: string, default: my-authorization, "propertyOrder": 3},
								authorization:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "authorization\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/authorization", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-authorization-4.1.2.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								}
							},
							required:[repo,authorization],
							"additionalProperties": false
						},
						{
							"title": "GATEWAY",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								repo:{title: "\/repo", type: string, default: my-gateway, "propertyOrder": 3},
								gateway:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "gateway\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/gateway", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-gateway-4.1.2.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								}
							},
							required:[repo,gateway],
							"additionalProperties": false
						},
						{
							"title": "EVENTHANDLER",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								repo:{title: "\/repo", type: string, default: my-eventhandler, "propertyOrder": 3},
								eventhandler:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "eventhandler\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/eventhandler", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-eventhandler-4.1.2.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								}
							},
							required:[repo,eventhandler],
							"additionalProperties": false
						},
						{
							"title": "GATEKEEPER",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								repo:{title: "\/repo", type: string, default: my-gatekeeper, "propertyOrder": 3},
								gatekeeper:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "gatekeeper\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/gatekeeper", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-gatekeeper-4.1.2.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								}
							},
							required:[repo,gatekeeper],
							"additionalProperties": false
						},
						{
							"title": "ORCHESTRATOR",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},
								repo:{title: "\/repo", type: string, default: my-orchestrator, "propertyOrder": 3},
								orchestrator:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "orchestrator\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/orchestrator", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-orchestrator-4.1.2.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								}
							},
							required:[repo, orchestrator],
							"additionalProperties": false
						}
					]
				},
				"minItems": 1,
				"uniqueItems": true
			},
			ArrowHead:
			{
				type: object,
				format: grid,
				properties:
				{
					Repo:
					{
						type: object,
						format: grid,
						properties:
						{
							git:
							{
								type: string, default: "https:\/\/github.com\/arrowhead-f\/core-java.git"
							},
							public:
							{
								type: boolean, default: true, format: checkbox
							}
						}
					}
				}
			}
		}
	},
	startval:
	{
		NameSpace:
		{
			name: true
		},
		Compile:
		{
			image:
			{
				fromDockerHub: true,
				namespace: "maven",
				repo: "",
				tag: "3-alpine"
			},
			imageArgs:"-v maven-repo:\/root\/.m2",
			command:"mvn",
			args: ""
		},
		Images:
		[
			{
				repo: my-database, 
				database: true,
				buildImage: one1
			},
			{
				repo: my-registry, 
				registry: true, 
				buildImage: "one1",
				artifacts_path: "serviceregistry_sql\/target\/",
				workdir: "\/arrowhead\/serviceregistry_sql",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-serviceregistry-sql-4.1.2.jar", "-d", "-daemon" ]				
			},
			{
				repo: my-authorization, 
				authorization:true, 
				buildImage: "one1",
				artifacts_path: "authorization\/target\/",
				workdir: "\/arrowhead\/authorization",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-authorization-4.1.2.jar", "-d", "-daemon" ]
			},
			{
				repo: my-gateway, 
				gateway:true, 
				buildImage: "one1",
				artifacts_path: "gateway\/target\/",
				workdir: "\/arrowhead\/gateway",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-gateway-4.1.2.jar", "-d", "-daemon" ]
			},
			{
				repo: my-eventhandler, 
				eventhandler:true, 
				buildImage: "one1",
				artifacts_path: "eventhandler\/target\/",
				workdir: "\/arrowhead\/eventhandler",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-eventhandler-4.1.2.jar", "-d", "-daemon" ]
			},
			{
				repo: my-gatekeeper, 
				gatekeeper: true, 
				buildImage: "one1",
				artifacts_path: "gatekeeper\/target\/",
				workdir: "\/arrowhead\/gatekeeper",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-gatekeeper-4.1.2.jar", "-d", "-daemon" ]
			},
			{
				repo: my-orchestrator, 
				orchestrator: true, 
				buildImage: "one1",
				artifacts_path: "orchestrator\/target\/",
				workdir: "\/arrowhead\/orchestrator",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-orchestrator-4.1.2.jar", "-d", "-daemon" ]				
			}
		],
		ArrowHead:
		{
			Repo:
			{
				git: "https:\/\/github.com\/arrowhead-f\/core-java.git",
				public: true
			}
		}
	}
}/);

def getCredentials(credId = "portus_api_token", repoId = "REPO_URL", userId = "PORTUS_USER", folder = "")
{
	def credentials = [:]
	
	def folderProperties = [:]
	
	Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each
	{
		f ->
			if( f.getParent() instanceof hudson.model.Hudson )
			{
				if( ! f.name.equals( folder ) )
				{
					return
				}
				
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
				
				if( property )
				{
					
					creds = property.getCredentials(UsernamePasswordCredentialsImpl.class)
					
					if( creds )
					{
						creds.each
						{
							f1 ->
								
									if( f1.getId().trim().equals(credId) )
										credentials.put(f1.getId(), ["user": f1.getUsername(), "pass": hudson.util.Secret.toString(f1.getPassword())])
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
				
				if( ! f.name.equals( folder ) )
				{
					return
				}
				
				AbstractFolder<?> folderAbs = AbstractFolder.class.cast(f)
				FolderProperties property = folderAbs.getProperties().get(FolderProperties.class)
				
				if( property )
				{
					property.properties.each
					{
						f1 -> 
							folderProperties.put( f1.getKey(), f1.getValue() )
					}
				}
			}
	}
	
	return ["creds": credentials, "folder" : folderProperties]
	
}

def portusApiGET(url, api, user, token)
{
	def url2 = url + api
	def get = new URL(url2).openConnection();
	
	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${user}:${token}")
	
  	//~ println user 
  
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


def baseFolderProperties = getCredentials("portus_api_token", "REPO_URL", "PORTUS_USER", base.trim())
def url = baseFolderProperties.folder.REPO_URL
def user = baseFolderProperties.creds.portus_api_token.user
def pass = baseFolderProperties.creds.portus_api_token.pass

def namespaces = portusApiGET(url, "/api/v1/namespaces?all=true", user, pass)

def repos = portusApiGET(url, "/api/v1/repositories?all=true", user, pass)

def teams = portusApiGET(url, "/api/v1/teams?all=true", user, pass)

def cloudNameSpaces = []

def buildImages = []

def mysqlBuildImages = []

def javaBuildImages = []

def mavenBuildImages = []

def TYPE_JAVA_TAG_ID = "javbt";
def TYPE_MYSQL_TAG_ID = "mysqlbt";
def TYPE_POSTGRES_TAG_ID = "pgrsbt";
def TYPE_OTHER_TAG_ID = "otherbt";
def TYPE_MAVEN_TAG_ID = "mvnbt";

namespaces.each
{
	namespace ->
  		if( namespace.global == true )
			return false
				
		cloudNameSpaces.add( namespace.name )
		
}

repos.each
{
	repo ->
			
		if( repo.namespace.name.startsWith("cpsiot-build") )
		{
			def image = repo.namespace.name + "/" + repo.name
			
			def tags = portusApiGET(url, "/api/v1/repositories/"+repo.id+"/tags", user, pass)
			
			tags.each
			{
				tag ->
					
					if( tag.name.contains( TYPE_JAVA_TAG_ID ) )
						javaBuildImages.add( image + ":" + tag.name )
					
					if( tag.name.contains( TYPE_MYSQL_TAG_ID ) )
						mysqlBuildImages.add( image + ":" + tag.name )
					
					if( tag.name.contains( TYPE_MAVEN_TAG_ID ) )
						mavenBuildImages.add( image + ":" + tag.name )
					
					buildImages.add(image + ":" + tag.name)
			}
		}
		
}

jsonEditorOptions.schema.properties.NameSpace.oneOf[0].properties.team.oneOf = []

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
		jsonEditorOptions.schema.properties.NameSpace.oneOf[0].properties.team.oneOf.add( temp )
}

jsonEditorOptions.schema.properties.NameSpace.oneOf[1].properties.name.enum = cloudNameSpaces

jsonEditorOptions.schema.properties.Compile.properties.image.oneOf[1].properties.name.enum = mavenBuildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[0].properties.buildImage.enum = mysqlBuildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[1].properties.buildImage.enum = javaBuildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[2].properties.buildImage.enum = javaBuildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[3].properties.buildImage.enum = javaBuildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[4].properties.buildImage.enum = javaBuildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[5].properties.buildImage.enum = javaBuildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[6].properties.buildImage.enum = javaBuildImages

if( mavenBuildImages )
	jsonEditorOptions.startval.Compile.image.name = mavenBuildImages[0]
else
	jsonEditorOptions.startval.Compile.image.name = ""

if( cloudNameSpaces )
	jsonEditorOptions.startval.NameSpace.name = cloudNameSpaces[0]
else
	jsonEditorOptions.startval.NameSpace.name = ""

if( mysqlBuildImages && javaBuildImages )
{
	jsonEditorOptions.startval.Images[0].buildImage = mysqlBuildImages[0]
	jsonEditorOptions.startval.Images[1].buildImage = javaBuildImages[0]
	jsonEditorOptions.startval.Images[2].buildImage = javaBuildImages[0]
	jsonEditorOptions.startval.Images[3].buildImage = javaBuildImages[0]
	jsonEditorOptions.startval.Images[4].buildImage = javaBuildImages[0]
	jsonEditorOptions.startval.Images[5].buildImage = javaBuildImages[0]
	jsonEditorOptions.startval.Images[6].buildImage = javaBuildImages[0]
}
else
{
	jsonEditorOptions.startval.Images[0].buildImage = ""
	jsonEditorOptions.startval.Images[1].buildImage = ""
	jsonEditorOptions.startval.Images[2].buildImage = ""
	jsonEditorOptions.startval.Images[3].buildImage = ""
	jsonEditorOptions.startval.Images[4].buildImage = ""
	jsonEditorOptions.startval.Images[5].buildImage = ""
	jsonEditorOptions.startval.Images[6].buildImage = ""
}

return jsonEditorOptions
