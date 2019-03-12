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
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: user }
													]
												},
												db_password:
												{
													"propertyOrder": 40,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: mypass }
													]
												},
												db_address:
												{
													"propertyOrder": 50,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: myadress }
													]
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:{type: string, "propertyOrder": 5},
												insecure_port:{type: integer, "propertyOrder": 6},
												secure_port:{type: integer, "propertyOrder": 7},
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
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: user }
													]
												},
												db_password:
												{
													"propertyOrder": 40,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: mypass }
													]
												},
												db_address:
												{
													"propertyOrder": 50,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: myadress }
													]
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:{type: string, "propertyOrder": 5, default: my-authorization},
												insecure_port:{type: integer, "propertyOrder": 6, default: 8444},
												secure_port:{type: integer, "propertyOrder": 7, default: 8445}
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
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: my-registry }
													]
												},
												sr_insecure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8442 }
													]
												},
												sr_secure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8443 }
													]
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
												address:{type: string, "propertyOrder": 5},
												insecure_port:{type: integer, "propertyOrder": 6},
												secure_port:{type: integer, "propertyOrder": 7},
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
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: my-registry }
													]
												},
												sr_insecure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8442 }
													]
												},
												sr_secure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8443 }
													]
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
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: user }
													]
												},
												db_password:
												{
													"propertyOrder": 40,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: mypass }
													]
												},
												db_address:
												{
													"propertyOrder": 50,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: myadress }
													]
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:{type: string, "propertyOrder": 5},
												insecure_port:{type: integer, "propertyOrder": 6},
												secure_port:{type: integer, "propertyOrder": 7}
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
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: my-registry }
													]
												},
												sr_insecure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8442 }
													]
												},
												sr_secure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8443 }
													]
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
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: user }
													]
												},
												db_password:
												{
													"propertyOrder": 40,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: mypass }
													]
												},
												db_address:
												{
													"propertyOrder": 50,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: myadress }
													]
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:{type: string, "propertyOrder": 5, default: my-gatekeeper},
												internal_insecure_port:{type: integer, default: 8446},
												internal_secure_port:{type: integer, default: 8447},
												external_insecure_port:{type: integer, default: 8448},
												external_secure_port:{type: integer, default: 8449}
											}
										},
										Security:
										{
											type: object,
											format: grid,
											properties:
											{												
												gatekeeper_keystore:{type: string, default: "config\/certificates\/gatekeeper.testcloud1.jks"},
												gatekeeper_keystore_pass:{type: string, default: "12345"},
												gatekeeper_keypass:{type: string, default: "12345"},
												cloud_keystore:{ type: string, default: "config\/certificates\/testcloud1_cert.jks"},
												cloud_keystore_pass:{type: string, default: "12345"},
												cloud_keypass: {type: string, default: "12345"},
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
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: my-registry }
													]
												},
												sr_insecure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8442 }
													]
												},
												sr_secure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8443 }
													]
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
													oneOf:
													[
														{ title: "From Orchestrator Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: my-orchestrator }
													]
												},
												orch_insecure_port:
												{
													oneOf:
													[
														{ title: "From Orchestrator Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8440 }
													]
												},
												orch_secure_port:
												{
													oneOf:
													[
														{ title: "From Orchestrator Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8441 }
													]
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
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: user }
													]
												},
												db_password:
												{
													"propertyOrder": 40,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: mypass }
													]
												},
												db_address:
												{
													"propertyOrder": 50,
													oneOf:
													[
														{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: myadress }
													]
												}
											}
										},
										Network:
										{
											type: object,
											format: grid,
											properties:
											{
												address:{type: string, "propertyOrder": 5, default: my-orchestrator},
												insecure_port:{type: integer, "propertyOrder": 6, default: 8440},
												secure_port:{type: integer, "propertyOrder": 7, default: 8441}
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
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: string, default: my-registry }
													]
												},
												sr_insecure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8442 }
													]
												},
												sr_secure_port:
												{
													oneOf:
													[
														{ title: "From SR Settings", type: boolean, readOnly: true, default: true, format: checkbox },
														{ title: "Input", type: integer, default: 8443 }
													]
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
								oneOf:
								[
									{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
									{ title: "Input", type: string, default: "jdbc:mysql:\/\/server:port\/log_db" }
								]
							},
							"log4j.appender.DB.user":
							{
								"propertyOrder": 51,
								oneOf:
								[
									{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
									{ title: "Input", type: string, default: log-db-user }
								]
							},
							"log4j.appender.DB.password":
							{
								"propertyOrder": 52,
								oneOf:
								[
									{ title: "From DB Settings", type: boolean, readOnly: true, default: true, format: checkbox },
									{ title: "Input", type: string, default: log-db-user-psw }
								]
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
				properties:
				{
					arrowheadUser: { type: string, default: "cpsiot", "propertyOrder": 5},
					arrowheadUserPWD: { type: string, default: "20cpsiot18", "propertyOrder": 6 },
					arrowHeadDB: {type: string, default: "cpsiot", "propertyOrder": 8},
					arrowHeadLogDB: {type: string, default: log, "propertyOrder": 9}
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
				fromCpsiot: true,
				name: "test\/test:1"
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
				Settings:
				{
					DB:
					{
						db_address: true
					},
					Security:
					{
						keystore: "config\/certificates\/serviceregistry.testcloud1.jks",
						keystorepass: "12345",
						keypass: "12345",
						truststore: "config\/certificates\/testcloud1_cert.jks",
						truststorepass: "12345"
					},
					Network:
					{
						address: my-registry,
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
				repo: my-authorization, authorization:true, buildImage: "one1",
				Settings:
				{
					DB:
					{
						db_address: true
					},
					Security:
					{
						keystore: "config\/certificates\/serviceregistry.testcloud1.jks",
						keystorepass: "12345",
						keypass: "12345",
						truststore: "config\/certificates\/testcloud1_cert.jks",
						truststorepass: "12345"
					},
					Network:
					{
						address: my-registry,
						insecure_port: 8444,
						secure_port: 8445						
					},
					ServiceRegistry:
					{
						sr_address: true,
						sr_insecure_port: true,
						sr_secure_port: true
					},
					Auth:
					{
						enable_auth_for_cloud: false
					}
				}
			},
			{
				repo: my-gateway, gateway:true, buildImage: "one1",
				Settings:
				{					
					Security:
					{
						keystore: "config\/certificates\/serviceregistry.testcloud1.jks",
						keystorepass: "12345",
						keypass: "12345",
						truststore: "config\/certificates\/testcloud1_cert.jks",
						truststorepass: "12345",
						trustpass: "12345",
						master_arrowhead_cert: "config\/certificates\/master_arrowhead_cert.crt"
					},
					Network:
					{
						address: my-gateway,
						insecure_port: 8452,
						secure_port: 8453,
						min_port: 8000,
						max_port: 8100
					},
					ServiceRegistry:
					{
						sr_address: true,
						sr_insecure_port: true,
						sr_secure_port: true
					}
				}
			},
			{
				repo: my-eventhandler, eventhandler:true, buildImage: "one1",
				Settings:
				{
					DB:
					{
						db_address: true
					},
					Security:
					{
						keystore: "config\/certificates\/serviceregistry.testcloud1.jks",
						keystorepass: "12345",
						keypass: "12345",
						truststore: "config\/certificates\/testcloud1_cert.jks",
						truststorepass: "12345"
					},
					Network:
					{
						address: my-eventhandler,
						insecure_port: 8454,
						secure_port: 8455
					},
					ServiceRegistry:
					{
						sr_address: true,
						sr_insecure_port: true,
						sr_secure_port: true
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
				repo: my-gatekeeper, gatekeeper: true, buildImage: "one1",
				Settings:
				{
					DB:
					{
						db_address: true
					},
					Network:
					{
						address: my-gatekeeper,
						internal_insecure_port: 8446,
						internal_secure_port: 8447,
						external_insecure_port: 8448,
						external_secure_port: 8449
					},
					Security:
					{
						gatekeeper_keystore: "config\/certificates\/gatekeeper.testcloud1.jks",
						gatekeeper_keystore_pass: "12345",
						gatekeeper_keypass: "12345",
						cloud_keystore: "config\/certificates\/testcloud1_cert.jks",
						cloud_keystore_pass: "12345",
						cloud_keypass: "12345",
						master_arrowhead_cert: "config\/certificates\/master_arrowhead_cert.crt"
					},
					ServiceRegistry:
					{
						sr_address: true,
						sr_insecure_port: true,
						sr_secure_port: true
					},
					Orchestrator:
					{
						orch_address: true,
						orch_insecure_port: true,
						orch_secure_port: true
					},
					Other:
					{
						timeout: 30000,
						use_gateway: false
					}								
				}
			},
			{
				repo: my-orchestrator, orchestrator: true, buildImage: "one1",
				Settings:
				{
					DB:
					{
						db_address: true
					},
					Security:
					{
						keystore: "config\/certificates\/serviceregistry.testcloud1.jks",
						keystorepass: "12345",
						keypass: "12345",
						truststore: "config\/certificates\/testcloud1_cert.jks",
						truststorepass: "12345"
					},
					Network:
					{
						address: my-orchestrator,
						insecure_port: 8440,
						secure_port: 8441						
					},
					ServiceRegistry:
					{
						sr_address: true,
						sr_insecure_port: true,
						sr_secure_port: true
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
				"log4j.appender.DB.URL":						true,
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
			arrowHeadDB: "cpsiot",
			arrowHeadLogDB: "log"
		}
	}
}/);

getCredentials()
def namespaces = portusApiGET("/api/v1/namespaces?all=true")

jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.cloud.enum = []

def cloudNameSpaces = []

namespaces.each
{
	namespace ->
  		if( namespace.global == true )
			return false
		
		if( ! namespace.name.startsWith("cloud") )
			return false
		
		cloudNameSpaces.add( namespace.name )
		
}

jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.cloud.enum = cloudNameSpaces

//~ jsonEditorOptions.startval.Namespace.name = 
//~ [ 
	//~ "id": startNameSpace.id, "name": startNameSpace.name, "description": (startNameSpace.description ? startNameSpace.description : ""), 
	//~ "team": ( startNameSpace.team ? startNameSpace.team.name : ""), "visibility": startNameSpace.visibility
//~ ]

//~ jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].oneOf = []

//~ teams.each
//~ {
	//~ team -> 
			
		//~ def props = [
						//~ "id": ["type": "integer", "default": team.id, "readOnly": true],
						//~ "name": ["type": "string", "default": team.name, "readOnly": true],
						//~ "description": ["type": "string", "default": (team.description ? team.description : "") , "readOnly": true],						
						//~ "hidden": ["type": "boolean", "default": team.hidden, "readOnly": true, "format": "checkbox"]
					//~ ]

  		//~ def temp = ["title": team.name, "type": "object", "format": "grid", "propertyOrder": 1, "properties": props ]
		//~ jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].oneOf.add( temp )
//~ }

//~ jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].enum = getTeamsFromData( teams , "V")
//~ jsonEditorOptions.schema.properties.Namespace.oneOf[1].properties.team.oneOf[0].options.enum_titles = getTeamsFromData( teams , "O")

//~ jsonEditorOptions.schema.properties.Portus.properties.namespace.oneOf[0].options.enum_titles = getNameSpacesFromData( namespaces , "O")
//~ jsonEditorOptions.schema.properties.Portus.properties.team.oneOf[1].options.enum_titles = getTeamsFromData( teams , "O")
//~ jsonEditorOptions.startval.Namespace.name = getNameSpacesFromData( namespaces , "V1")

return jsonEditorOptions
