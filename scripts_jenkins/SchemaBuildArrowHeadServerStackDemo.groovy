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
				format: grid,
				oneOf:
				[
					{
						title: "New Cloud",
						properties:
						{					
				
							cloud:
							{
								
								type: string, default: "",
								description: "New Namespace for this cloud under which the whole stack will be pushed"
							},
							new:
							{
								type: boolean, readOnly:true, default: true, format: checkbox, options:{hidden: true}
							}
						},
						"additionalProperties": false,
						required:[cloud, new]
					},
					{
						title: "Existing Cloud",
						properties:
						{
							cloud:
							{
								type: string, enum:[one, two,three],
								description: "Images will be pushed under this namespace"
							}
						},
						"additionalProperties": false,
						required:[cloud]
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
								database:{type: boolean, default: true, readOnly: true, format: checkbox, options:{ hidden: true}},
								initDBScript:
								{
									oneOf:
									[
										{
											type: object,
											title: Script Path in Repo,
											properties:
											{
												initDBScriptPath:
												{
													type: string
												}
											}
										},
										{
											title: Generate,
											type: boolean, readOnly: true, format: checkbox
										},
										{
											type: object,
											title: Input,
											properties:
											{
												initDBScriptInput:
												{
													type: string, format: textarea
												}
											}
										}
									]								
								}
							},
							required:[repo,database],
							"additionalProperties": false
						},
						{
							"title": "SERVICE_REGISTRY",
							properties:
							{
								buildImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								repo:{title: "\/repo", type: string, default: my-database, "propertyOrder": 3},
								registry:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "serviceregistry_sql\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/serviceregistry_sql", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "serviceregistry_sql-4.0.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								},
								Settings:
								{
									type: object,
									format: categories,
									properties:
									{
										DB:
										{
											type: object,
											format: grid,
											properties:
											{
												db_user:
												{
													"propertyOrder": 30,
													title: "User from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_password:
												{
													"propertyOrder": 40,
													title: "Password From Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_address:
												{
													"propertyOrder": 50,
													type: string, default: "", "propertyOrder": 9, readOnly: true,
													"template": "jdbc:mysql:\/\/{{namespace}}-db-server:3306\/{{db}}",
													"watch":{ "namespace": "root.NameSpace.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:
												{
													"propertyOrder": 50,
													type: string, default: "", "propertyOrder": 9, readOnly: true,
													"template": "{{namespace}}-service-registry",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												insecure_port:
												{
													type: integer, "propertyOrder": 6, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.insecure_port"}
												},
												secure_port:
												{
													type: integer, "propertyOrder": 7, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.secure_port"}
												},
												ping_scheduled:{type: boolean, format: checkbox, "propertyOrder": 16},
												ping_timeout:{type: integer, "propertyOrder": 8},
												ping_interval:{type: integer, "propertyOrder": 9},
												ttl_scheduled:{type: boolean, format: checkbox, "propertyOrder": 17},
												ttl_interval:{type: integer, "propertyOrder": 11}
											}
										},
										Security:
										{
											type: object,
											format: grid,
											properties:
											{
												keystore:{type: string, "propertyOrder": 20},
												keystorepass:{type: string, "propertyOrder": 21},
												keypass:{type: string, "propertyOrder": 22},
												truststore:{type: string, "propertyOrder": 23},
												truststorepass:{type: string, "propertyOrder": 24}
											}
										}
									}
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
								repo:{title: "\/repo", type: string, default: my-database, "propertyOrder": 3},
								authorization:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "authorization\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/authorization", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "authorization-4.0.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								},
								Settings:
								{
									type: object,
									format: categories,
									properties:
									{
										DB:
										{
											type: object,
											format: grid,
											properties:
											{
												db_user:
												{
													"propertyOrder": 30,
													title: "User from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_password:
												{
													"propertyOrder": 40,
													title: "Password from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_address:
												{
													"propertyOrder": 50,
													type: string, default: "", "propertyOrder": 9, readOnly: true,
													"template": "jdbc:mysql:\/\/{{namespace}}-db-server:3306\/{{db}}",
													"watch":{ "namespace": "root.NameSpace.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:
												{
													"propertyOrder": 50, type: string, default: "", readOnly: true,
													"template": "{{namespace}}-authorization",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												insecure_port:
												{
													type: integer, "propertyOrder": 6, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.Authorization.insecure_port"}
													
												},
												secure_port:
												{
													type: integer, "propertyOrder": 7,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.Authorization.secure_port"}
												}
											}
										},
										ServiceRegistry:
										{
											type: object,
											format: grid,
											properties:
											{
												sr_address:
												{
													type: string, readOnly: true,
													"template": "{{namespace}}-service-registry",
													"watch":{ "namespace": "root.NameSpace.cloud"}													
												},
												sr_insecure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.insecure_port"}
												},
												sr_secure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.secure_port"}
												}
											}
										},
										Auth:
										{
											type: object,
											format: grid,
											properties:
											{
												enable_auth_for_cloud:{type: boolean, "propertyOrder": 5, format: checkbox}
											}
										}
										Security:
										{
											type: object,
											format: grid,
											properties:
											{
												keystore:{type: string, "propertyOrder": 20},
												keystorepass:{type: string, "propertyOrder": 21},
												keypass:{type: string, "propertyOrder": 22},
												truststore:{type: string, "propertyOrder": 23},
												truststorepass:{type: string, "propertyOrder": 24}
											}
										}
									}
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
								repo:{title: "\/repo", type: string, default: my-database, "propertyOrder": 3},
								gateway:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "gateway\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/gateway", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "gateway-4.0.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								},
								Settings:
								{
									type: object,
									format: categories,
									properties:
									{
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:
												{
													type: string, "propertyOrder": 5, default: "", readOnly: true,
													"template": "{{namespace}}-gateway",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												insecure_port:
												{
													type: integer, "propertyOrder": 6, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.GateWay.insecure_port"}
													
												},
												secure_port:
												{
													type: integer, "propertyOrder": 7,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.GateWay.secure_port"}
												},
												min_port:{type: integer, "propertyOrder": 8},
												max_port:{type: integer, "propertyOrder": 9}
											}
										},
										Security:
										{
											type: object,
											format: grid,
											properties:
											{
												keystore:{type: string, "propertyOrder": 20},
												keystorepass:{type: string, "propertyOrder": 21},
												keypass:{type: string, "propertyOrder": 22},
												truststore:{type: string, "propertyOrder": 23},
												truststorepass:{type: string, "propertyOrder": 24}
												trustpass:{type: string, "propertyOrder": 25},
												master_arrowhead_cert:{type: string, "propertyOrder": 26}
											}
										},
										ServiceRegistry:
										{
											type: object,
											format: grid,
											properties:
											{
												sr_address:
												{
													type: string, readOnly: true,
													"template": "{{namespace}}-service-registry",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												sr_insecure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.insecure_port"}
												},
												sr_secure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.secure_port"}
												}
											}
										}
									}
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
								repo:{title: "\/repo", type: string, default: my-database, "propertyOrder": 3},
								eventhandler:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "eventhandler\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/eventhandler", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "eventhandler-4.0.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								},
								Settings:
								{
									type: object,
									format: categories,
									properties:
									{
										DB:
										{
											type: object,
											format: grid,
											properties:
											{
												db_user:
												{
													"propertyOrder": 30,
													title: "User from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_password:
												{
													"propertyOrder": 40,
													title: "Password from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_address:
												{
													"propertyOrder": 50,
													type: string, default: "", "propertyOrder": 9, readOnly: true,
													"template": "jdbc:mysql:\/\/{{namespace}}-db-server:3306\/{{db}}",
													"watch":{ "namespace": "root.NameSpace.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:
												{
													type: string, "propertyOrder": 5, default: "",
													"template": "{{namespace}}-eventhandler",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												insecure_port:
												{
													type: integer, "propertyOrder": 6, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.EventHandler.insecure_port"}
													
												},
												secure_port:
												{
													type: integer, "propertyOrder": 7,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.EventHandler.secure_port"}
												}
											}
										},
										Security:
										{
											type: object,
											format: grid,
											properties:
											{
												keystore:{type: string, "propertyOrder": 20},
												keystorepass:{type: string, "propertyOrder": 21},
												keypass:{type: string, "propertyOrder": 22},
												truststore:{type: string, "propertyOrder": 23},
												truststorepass:{type: string, "propertyOrder": 24}
											}
										},
										Other:
										{
											type: object,
											format: grid,
											properties:
											{
												event_publishing_tolerance:{type: integer, default: 60},
												remove_old_filters:{type: boolean, default: false, format: checkbox},
												check_interval:{type: integer, default: 60 }
											}
											
										},
										ServiceRegistry:
										{
											type: object,
											format: grid,
											properties:
											{
												sr_address:
												{
													type: string, readOnly: true,
													"template": "{{namespace}}-service-registry",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												sr_insecure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.insecure_port"}
												},
												sr_secure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.secure_port"}
												}
											}
										}
									}
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
								repo:{title: "\/repo", type: string, default: my-database, "propertyOrder": 3},
								gatekeeper:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "gatekeeper\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/gatekeeper", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "gatekeeper-4.0.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								},
								Settings:
								{
									type: object,
									format: categories,
									properties:
									{
										DB:
										{
											type: object,
											format: grid,
											properties:
											{
												db_user:
												{
													"propertyOrder": 30,
													title: "User from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_password:
												{
													"propertyOrder": 40,
													title: "Password from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_address:
												{
													"propertyOrder": 50,
													type: string, default: "", "propertyOrder": 9, readOnly: true,
													"template": "jdbc:mysql:\/\/{{namespace}}-db-server:3306\/{{db}}",
													"watch":{ "namespace": "root.NameSpace.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:
												{
													type: string, "propertyOrder": 5, default: "",
													"template": "{{namespace}}-gatekeeper",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												internal_insecure_port:
												{
													type: integer, "propertyOrder": 6, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.GateKeeper.internal_insecure_port"}
												},
												internal_secure_port:
												{
													type: integer, "propertyOrder": 7, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.GateKeeper.internal_secure_port"}
												},
												external_address:
												{
													type: string, "propertyOrder": 8, default: "0.0.0.0"
												},
												external_insecure_port:
												{
													type: integer, "propertyOrder": 9, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.GateKeeper.external_insecure_port"}
												},
												external_secure_port:
												{
													type: integer, "propertyOrder": 10, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.GateKeeper.external_secure_port"}
												}
												
											}
										},
										Security:
										{
											type: object,
											format: grid,
											properties:
											{												
												gatekeeper_keystore:{type: string, default: "config\/certificates\/gatekeeper.testcloud1.jks"},
												gatekeeper_keystore_pass:{type: string, default: "123456"},
												gatekeeper_keypass:{type: string, default: "123456"},
												cloud_keystore:{ type: string, default: "config\/certificates\/testcloud1_cert.jks"},
												cloud_keystore_pass:{type: string, default: "123456"},
												cloud_keypass: {type: string, default: "123456"},
												master_arrowhead_cert:{type: string, default: "config\/certificates\/master_arrowhead_cert.crt"}
											}
										},
										ServiceRegistry:
										{
											type: object,
											format: grid,
											properties:
											{
												sr_address:
												{
													type: string, readOnly: true,
													"template": "{{namespace}}-service-registry",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												sr_insecure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.insecure_port"}
												},
												sr_secure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.secure_port"}
												}
											}
										},
										Orchestrator:
										{
											type: object,
											format: grid,
											properties:
											{
												orch_address:
												{
													type: string, readOnly: true,
													"template": "{{namespace}}-orchestrator",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												orch_insecure_port:
												{
													type: integer, "propertyOrder": 6, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.Orchestrator.insecure_port"}
												},
												orch_secure_port:
												{
													type: integer, "propertyOrder": 7,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.Orchestrator.secure_port"}
												}
											}
										},
										Other:
										{
											type: object,
											format: grid,
											properties:
											{
												timeout:{type: integer, default: 30000},
												use_gateway:{type: boolean, default: false, format: checkbox}
											}											
										}
									}
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
								repo:{title: "\/repo", type: string, default: my-database, "propertyOrder": 3},
								orchestrator:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								artifacts_path: { type: string, default: "orchestrator\/target\/", "propertyOrder": 4},
								workdir:{type: string, default: "\/arrowhead\/orchestrator", "propertyOrder": 5},
								entry_point:
								{
									"propertyOrder": 6,
									type: array, items:{ type: string, "headerTemplate": "{{ self }}"},
									default: ["java", "-cp", "lib\/*:*", "-jar", "orchestrator-4.0.jar", "-d", "-daemon" ],
									"propertyOrder": 5,
									format: tabs-top
								},
								Settings:
								{
									type: object,
									format: categories,
									properties:
									{
										DB:
										{
											type: object,
											format: grid,
											properties:
											{
												db_user:
												{
													"propertyOrder": 30,
													title: "User from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_password:
												{
													"propertyOrder": 40,
													title: "Password from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
												},
												db_address:
												{
													"propertyOrder": 50,
													type: string, default: "", "propertyOrder": 9, readOnly: true,
													"template": "jdbc:mysql:\/\/{{namespace}}-db-server:3306\/{{db}}",
													"watch":{ "namespace": "root.NameSpace.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:
												{
													type: string, "propertyOrder": 5, default: "",
													"template": "{{namespace}}-orchestrator",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												insecure_port:
												{
													type: integer, "propertyOrder": 6, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.Orchestrator.insecure_port"}
													
												},
												secure_port:
												{
													type: integer, "propertyOrder": 7,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.Orchestrator.secure_port"}
												}
											}
										},
										Security:
										{
											type: object,
											format: grid,
											properties:
											{
												keystore:{type: string, "propertyOrder": 20},
												keystorepass:{type: string, "propertyOrder": 21},
												keypass:{type: string, "propertyOrder": 22},
												truststore:{type: string, "propertyOrder": 23},
												truststorepass:{type: string, "propertyOrder": 24}
											}
										},
										ServiceRegistry:
										{
											type: object,
											format: grid,
											properties:
											{
												sr_address:
												{
													type: string, readOnly: true,
													"template": "{{namespace}}-service-registry",
													"watch":{ "namespace": "root.NameSpace.cloud"}
												},
												sr_insecure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.insecure_port"}
												},
												sr_secure_port:
												{
													type: integer, readOnly: true,
													"template": "{{port}}",
													"watch":{ "port": "root.ArrowHead.ServiceRegistry.secure_port"}
												}
											}
										}
									}
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
			Logger:
			{
				type: object,
				format: categories,
				properties:
				{
					"General":
					{
						"propertyOrder": 1,
						type: object,
						properties:
						{
							"log4j.rootLogger":{type: string, default: "DEBUG, DB"},
							"log4j.logger.org.hibernate":{type: string, default: "fatal"}
						}
					},
					"DB":
					{
						"propertyOrder": 2,
						type: object,
						properties:
						{
							"log4j.appender.DB":{type: string, default: "org.apache.log4j.jdbc.JDBCAppender"},
							"log4j.appender.DB.driver":{type: string, default: "com.mysql.jdbc.Driver"},
							"log4j.appender.DB.URL":
							{
								"propertyOrder": 50,
								type: string, default: "", readOnly: true,
								"template": "jdbc:mysql:\/\/{{namespace}}-db-server:3306\/{{db}}",
								"watch":{ "namespace": "root.NameSpace.cloud", "db": "root.ArrowHead.DB.arrowHeadLogDB"}						
							},
							"log4j.appender.DB.user":
							{
								"propertyOrder": 51,
								title: "User from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
							},
							"log4j.appender.DB.password":
							{
								"propertyOrder": 52,
								title: "Password from Build Parameters", type: boolean, readOnly: true, default: true, format: checkbox
							},
							"log4j.appender.DB.sql":{type: string, default: "INSERT INTO logs VALUES(DEFAULT,'%d{yyyy-MM-dd HH:mm:ss}','%C','%p','%m')"},
							"log4j.appender.DB.layout":{type: string, default: "org.apache.log4j.PatternLayout"}
						}
					},
					"File":
					{
						"propertyOrder": 3,
						type: object,
						properties:
						{
							"log4j.appender.FILE":{type: string, default: "org.apache.log4j.FileAppender"},
							"log4j.appender.FILE.File":{type: string, default: "log4j_log.txt"},
							"log4j.appender.FILE.ImmediateFlush":{type: boolean, format: checkbox, default: true},
							"log4j.appender.FILE.Threshold":{type: string, default: "debug"},
							"log4j.appender.FILE.Append":{type: boolean, format: checkbox, default: false},
							"log4j.appender.FILE.layout":{type: string, default: "org.apache.log4j.PatternLayout"},
							"log4j.appender.FILE.layout.conversionPattern":{type: string, default: "%d{yyyy-MM-dd HH:mm:ss}, %C, %p, %m%n"}
						}
					}
				}				
			},
			ArrowHead:
			{
				type: object,
				format: categories,
				properties:
				{
					DB:
					{
						"propertyOrder": 1,
						type: object,
						properties:
						{
							arrowHeadDB: {type: string, default: "cpsiot", "propertyOrder": 8},
							arrowHeadDBAdress:
							{
								type: string, default: "", "propertyOrder": 9, readOnly: true,
								"template": "{{namespace}}-db-server",
								"watch":{ "namespace": "root.NameSpace.cloud"}
							},
							arrowHeadLogDB: {type: string, default: log, "propertyOrder": 10},
							arrowHeadLogDBAdress:
							{
								type: string, default: "", "propertyOrder": 11, readOnly: true,
								"template": "{{namespace}}-db-server",
								"watch":{ "namespace": "root.NameSpace.cloud"}
							}
						}
					},
					ServiceRegistry:
					{
						type: object,
						properties:
						{
							insecure_port: { type: integer, default: 8442 },
							secure_port: { type: integer, default: 8443 }
						}
					},
					Authorization:
					{
						type: object,
						properties:
						{
							insecure_port: { type: integer, default: 8444 },
							secure_port: { type: integer, default: 8445 }
						}
					},
					EventHandler:
					{
						type: object,
						properties:
						{
							insecure_port: { type: integer, default: 8454 },
							secure_port: { type: integer, default: 8455 }
						}
					},
					GateKeeper:
					{
						type: object,
						properties:
						{
							internal_insecure_port: { type: integer, default: 8446 },
							internal_secure_port: { type: integer, default: 8447 },
							external_insecure_port: { type: integer, default: 8448 },
							external_secure_port: { type: integer, default: 8449 }
						}
					},
					GateWay:
					{
						type: object,
						properties:
						{
							insecure_port: { type: integer, default: 8452 },
							secure_port: { type: integer, default: 8453 }
						}
					},
					Orchestrator:
					{
						type: object,
						properties:
						{
							insecure_port: { type: integer, default: 8440 },
							secure_port: { type: integer, default: 8441 }
						}
					},
					Conf:
					{
						type: object,
						format: grid,
						oneOf:
						[
							{
								title: "Arrowhead 3",
								properties:
								{
									log4j:
									{
										type: string, default: "log4j.properties"
									},
									application:
									{
										type: string, default: "app.properties"
									},
									arrowHead3:
									{
										type: boolean, default: true, format: checkbox, readOnly: true, options:{hidden: true}
									}
								},
								"additionalProperties": false
							},
							{
								title: "Arrowhead 4",
								properties:
								{
									app:
									{
										type: string, default: "app.conf"
									},
									arrowHead4:
									{
										type: boolean, default: true, format: checkbox, readOnly: true, options:{hidden: true}
									}
								},
								"additionalProperties": false
							}
						]
					},
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
			cloud: ""
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
				buildImage: one1,
				initDBScript: true
			},
			{
				repo: my-registry, 
				registry: true, 
				buildImage: "one1",
				artifacts_path: "serviceregistry_sql\/target\/",
				workdir: "\/arrowhead\/serviceregistry_sql",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-serviceregistry-sql-4.1.2.jar", "-d", "-daemon" ],
				Settings:
				{
					DB:
					{
						db_address: "",
						db_user: true,
						db_password: true
					},
					Security:
					{
						keystore: "config\/certificates\/service_registry_sql.p12",
						keystorepass: "123456",
						keypass: "123456",
						truststore: "config\/certificates\/truststore.p12",
						truststorepass: "123456"
					},
					Network:
					{
						address: "0.0.0.0",
						insecure_port: 8442,
						secure_port: 8443,
						ping_scheduled: false,
						ping_timeout: 5000,
						ping_interval: 60,
						ttl_scheduled: false,
						ttl_interval: 10
					}
				}
			},
			{
				repo: my-authorization, 
				authorization:true, 
				buildImage: "one1",
				artifacts_path: "authorization\/target\/",
				workdir: "\/arrowhead\/authorization",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-authorization-4.1.2.jar", "-d", "-daemon" ],
				Settings:
				{
					DB:
					{
						db_address: "",
						db_user: true,
						db_password: true
					},
					Security:
					{
						keystore: "config\/certificates\/authorization.p12",
						keystorepass: "123456",
						keypass: "123456",
						truststore: "config\/certificates\/truststore.p12",
						truststorepass: "123456"
					},
					Network:
					{
						address: "0.0.0.0",
						insecure_port: 0,
						secure_port: 0						
					},
					ServiceRegistry:
					{
						sr_address: "",
						sr_insecure_port: 0,
						sr_secure_port: 0
					},
					Auth:
					{
						enable_auth_for_cloud: false
					}
				}
			},
			{
				repo: my-gateway, 
				gateway:true, 
				buildImage: "one1",
				artifacts_path: "gateway\/target\/",
				workdir: "\/arrowhead\/gateway",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-gateway-4.1.2.jar", "-d", "-daemon" ],
				Settings:
				{					
					Security:
					{
						keystore: "config\/certificates\/gateway.p12",
						keystorepass: "123456",
						keypass: "123456",
						truststore: "config\/certificates\/truststore.p12",
						truststorepass: "123456",
						trustpass: "123456",
						master_arrowhead_cert: "config\/certificates\/master.crt"
					},
					Network:
					{
						address: "0.0.0.0",
						insecure_port: 0,
						secure_port: 0,
						min_port: 8000,
						max_port: 8100
					},
					ServiceRegistry:
					{
						sr_address: "",
						sr_insecure_port: 0,
						sr_secure_port: 0
					}
				}
			},
			{
				repo: my-eventhandler, 
				eventhandler:true, 
				buildImage: "one1",
				artifacts_path: "eventhandler\/target\/",
				workdir: "\/arrowhead\/eventhandler",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-eventhandler-4.1.2.jar", "-d", "-daemon" ],
				Settings:
				{
					DB:
					{
						db_address: "",
						db_user: true,
						db_password: true
					},
					Security:
					{
						keystore: "config\/certificates\/event_handler.p12",
						keystorepass: "123456",
						keypass: "123456",
						truststore: "config\/certificates\/truststore.p12",
						truststorepass: "123456"
					},
					Network:
					{
						address: "0.0.0.0",
						insecure_port: 0,
						secure_port: 0
					},
					ServiceRegistry:
					{
						sr_address: "",
						sr_insecure_port: 0,
						sr_secure_port: 0
					},
					Other:
					{
						event_publishing_tolerance: 60,
						remove_old_filters: false,
						check_interval: 60
					}
				}
			},
			{
				repo: my-gatekeeper, 
				gatekeeper: true, 
				buildImage: "one1",
				artifacts_path: "gatekeeper\/target\/",
				workdir: "\/arrowhead\/gatekeeper",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-gatekeeper-4.1.2.jar", "-d", "-daemon" ],
				Settings:
				{
					DB:
					{
						db_address: "",
						db_user: true,
						db_password: true
					},
					Network:
					{
						address: "0.0.0.0",
						internal_insecure_port: 0,
						internal_secure_port: 0,
						external_insecure_port: 0,
						external_secure_port: 0,
						external_address: "0.0.0.0"
					},
					Security:
					{
						gatekeeper_keystore: "config\/certificates\/gatekeeper.p12",
						gatekeeper_keystore_pass: "123456",
						gatekeeper_keypass: "123456",
						cloud_keystore: "config\/certificates\/truststore.p12",
						cloud_keystore_pass: "123456",
						cloud_keypass: "123456",
						master_arrowhead_cert: "config\/certificates\/master.crt"
					},
					ServiceRegistry:
					{
						sr_address: "",
						sr_insecure_port: 0,
						sr_secure_port: 0
					},					
					Other:
					{
						timeout: 30000,
						use_gateway: true
					}								
				}
			},
			{
				repo: my-orchestrator, 
				orchestrator: true, 
				buildImage: "one1",
				artifacts_path: "orchestrator\/target\/",
				workdir: "\/arrowhead\/orchestrator",
				entry_point: ["java", "-cp", "lib\/*:*", "-jar", "arrowhead-orchestrator-4.1.2.jar", "-d", "-daemon" ],
				Settings:
				{
					DB:
					{
						db_address: "",
						db_user: true,
						db_password: true
					},
					Security:
					{
						keystore: "config\/certificates\/orchestrator.p12",
						keystorepass: "123456",
						keypass: "123456",
						truststore: "config\/certificates\/truststore.p12",
						truststorepass: "123456"
					},
					Network:
					{
						address: "0.0.0.0",
						insecure_port: 0,
						secure_port: 0						
					},
					ServiceRegistry:
					{
						sr_address: "",
						sr_insecure_port: 0,
						sr_secure_port: 0
					}
				}
			}
		],
		Logger:
		{
			"General":
			{
				"log4j.rootLogger": 							"DEBUG, DB",
				"log4j.logger.org.hibernate":					"fatal"
			},
			"DB":
			{
				"log4j.appender.DB": 							"org.apache.log4j.jdbc.JDBCAppender",
				"log4j.appender.DB.driver":						"com.mysql.jdbc.Driver",
				"log4j.appender.DB.URL":						"",
				"log4j.appender.DB.user":						true,
				"log4j.appender.DB.password":					true,
				"log4j.appender.DB.sql":						"INSERT INTO logs VALUES(DEFAULT,'%d{yyyy-MM-dd HH:mm:ss}','%C','%p','%m')",
				"log4j.appender.DB.layout":						"org.apache.log4j.PatternLayout"
			},
			"File":
			{
				"log4j.appender.FILE":							"org.apache.log4j.FileAppender",
				"log4j.appender.FILE.File":						"log4j_log.txt",
				"log4j.appender.FILE.ImmediateFlush":			true,
				"log4j.appender.FILE.Threshold":				"debug",
				"log4j.appender.FILE.Append":					false,
				"log4j.appender.FILE.layout":					"org.apache.log4j.PatternLayout",
				"log4j.appender.FILE.layout.conversionPattern":	"%d{yyyy-MM-dd HH:mm:ss}, %C, %p, %m%n"
			}
		},
		ArrowHead:
		{
			DB:
			{
				arrowHeadDB: "cpsiot",
				arrowHeadLogDB: "log",
				arrowHeadDBAdress: "",
				arrowHeadLogDBAdress: ""
			},
			ServiceRegistry:
			{
				insecure_port: 8442,
				secure_port: 8443
			},
			Authorization:
			{
				insecure_port: 8444,
				secure_port: 8445
			},
			EventHandler:
			{
				insecure_port: 8454,
				secure_port: 8455
			},
			GateKeeper:
			{
				internal_insecure_port: 8446,
				internal_secure_port: 8447,
				external_insecure_port: 8448,
				external_secure_port: 8449
			},
			GateWay:
			{
				insecure_port: 8452,
				secure_port: 8453
			},
			Orchestrator:
			{
				insecure_port: 8440,
				secure_port: 8441
			},
			Conf:
			{
				app: "app.conf",
				arrowHead4: true
			},
			Repo:
			{
				git: "https:\/\/github.com\/arrowhead-f\/core-java.git",
				public: true
			}
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

def repos = portusApiGET("/api/v1/repositories?all=true")

def cloudNameSpaces = []

def buildImages = []

namespaces.each
{
	namespace ->
  		if( namespace.global == true )
			return false
		
		if( namespace.name.startsWith("cloud") )
			cloudNameSpaces.add( namespace.name )
		
}

repos.each
{
	repo ->
			
		if( repo.namespace.name.startsWith("cpsiot-build") )
		{
			def image = repo.namespace.name + "/" + repo.name
			
			def tags = portusApiGET("/api/v1/repositories/"+repo.id+"/tags")
			
			tags.each
			{
				tag ->
					
					buildImages.add(image + ":" + tag.name)
			}
		}
		
}

jsonEditorOptions.schema.properties.NameSpace.oneOf[1].properties.cloud.enum = cloudNameSpaces

jsonEditorOptions.schema.properties.Compile.properties.image.oneOf[1].properties.name.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[0].properties.buildImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[1].properties.buildImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[2].properties.buildImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[3].properties.buildImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[4].properties.buildImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[5].properties.buildImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[6].properties.buildImage.enum = buildImages

jsonEditorOptions.startval.Compile.image.name = buildImages[0]

jsonEditorOptions.startval.NameSpace.cloud = cloudNameSpaces[0]

jsonEditorOptions.startval.Images[0].buildImage = buildImages[0]
jsonEditorOptions.startval.Images[1].buildImage = buildImages[0]
jsonEditorOptions.startval.Images[2].buildImage = buildImages[0]
jsonEditorOptions.startval.Images[3].buildImage = buildImages[0]
jsonEditorOptions.startval.Images[4].buildImage = buildImages[0]
jsonEditorOptions.startval.Images[5].buildImage = buildImages[0]
jsonEditorOptions.startval.Images[6].buildImage = buildImages[0]

return jsonEditorOptions
