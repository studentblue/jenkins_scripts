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

import hudson.util.RemotingDiagnostics;
import com.synopsys.arc.jenkins.plugins.ownership.nodes.NodeOwnerHelper;
import org.jenkinsci.plugins.ownership.model.folders.*;
import com.synopsys.arc.jenkins.plugins.ownership.OwnershipDescription;
import com.synopsys.arc.jenkins.plugins.ownership.util.OwnershipDescriptionHelper;

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
			Node:
			{
				"propertyOrder": 2,
				type: object,				
				oneOf:[]
			},
			Docker:
			{
				"propertyOrder": 2,
				type: object,
				properties:
				{					
		
					cloud:
					{
						oneOf:
						[
							{
								title: From Node,
								type: string, readOnly: true,
								description: "Get Network from Selected Node"
							},
							{
								title: New,
								type: string, default: "my-cloud",
								description: "Network Name for the Cloud"
							}
							
						]
					},
					delay:
					{
						type: integer, default: 10,
						description: "delay in seconds between image starts"
					}		
				},
				"additionalProperties": false,
				required:[cloud]
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
					"headerTemplate": "{{ self.name }}",
					"title": "Image",
					oneOf:
					[
						{
							"title": "DB",
							properties:
							{
								name:{ type: string, default: "DB", "propertyOrder": 100, options:{ hidden: true}},
								deployImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},
								database:{type: boolean, default: true, readOnly: true, format: checkbox, options:{ hidden: true}},
								initDBScript:
								{
									oneOf:
									[
										{
										
										title: input,
										type: string, default: "My SQL script here",
										description: "New Namespace for this stack under which all images will be pushed"
										},
										{
											title: generate,
											type: boolean, readOnly:true, default: true, format: checkbox,
											description: "Generate SQL Init for DB"
										}
									]								
								}
							},
							required:[database],
							"additionalProperties": false
						},
						{
							"title": "SERVICE_REGISTRY",
							properties:
							{
								name:{ type: string, default: "SERVICE_REGISTRY", "propertyOrder": 100, options:{ hidden: true}},
								deployImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								registry:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								workdir:{type: string, default: "\/arrowhead\/serviceregistry_sql", "propertyOrder": 5},						
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
													"watch":{ "namespace": "root.Docker.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
												keystore:{type: string, "propertyOrder": 20, default: "config\/certificates\/service_registry_sql.p12"},
												keystorepass:{type: string, "propertyOrder": 21, default: "123456"},
												keypass:{type: string, "propertyOrder": 22, default: "123456"},
												truststore:{type: string, "propertyOrder": 23, default: "config\/certificates\/truststore.p12"},
												truststorepass:{type: string, "propertyOrder": 24, default: "123456"}
											}
										}
									}
								}
							},
							required:
							[
								registry, deployImage
							],
							"additionalProperties": false
						},
						{
							"title": "AUTHORIZATION",
							properties:
							{
								name:{ type: string, default: "AUTHORIZATION", "propertyOrder": 100, options:{ hidden: true}},
								deployImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								authorization:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								workdir:{type: string, default: "\/arrowhead\/authorization", "propertyOrder": 5},						
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
													"watch":{ "namespace": "root.Docker.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
													"watch":{ "namespace": "root.Docker.cloud"}													
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
												keystore:{type: string, "propertyOrder": 20, default: "config\/certificates\/authorization.p12"},
												keystorepass:{type: string, "propertyOrder": 21, default: "123456"},
												keypass:{type: string, "propertyOrder": 22, default: "123456"},
												truststore:{type: string, "propertyOrder": 23, default: "config\/certificates\/truststore.p12"},
												truststorepass:{type: string, "propertyOrder": 24, default: "123456"}
												
											}
										}
									}
								}
							},
							required:[authorization],
							"additionalProperties": false
						},
						{
							"title": "GATEWAY",
							properties:
							{
								name:{ type: string, default: "GATEWAY", "propertyOrder": 100, options:{ hidden: true}},
								deployImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								gateway:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								workdir:{type: string, default: "\/arrowhead\/gateway", "propertyOrder": 5},								
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
												keystore:{type: string, "propertyOrder": 20, default: "config\/certificates\/gateway.p12"},
												keystorepass:{type: string, "propertyOrder": 21, default: "123456"},
												keypass:{type: string, "propertyOrder": 22, default: "123456"},
												truststore:{type: string, "propertyOrder": 23, default: "config\/certificates\/truststore.p12"},
												truststorepass:{type: string, "propertyOrder": 24, default: "123456"}
												trustpass:{type: string, "propertyOrder": 25, default: "123456"},
												master_arrowhead_cert:{type: string, "propertyOrder": 26, default: "config\/certificates\/master.crt"}												
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
							required:[gateway],
							"additionalProperties": false
						},
						{
							"title": "EVENTHANDLER",
							properties:
							{
								name:{ type: string, default: "EVENTHANDLER", "propertyOrder": 100, options:{ hidden: true}},
								deployImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								eventhandler:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								workdir:{type: string, default: "\/arrowhead\/eventhandler", "propertyOrder": 5},
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
													"watch":{ "namespace": "root.Docker.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
												keystore:{type: string, "propertyOrder": 20, default: "config\/certificates\/event_handler.p12"},
												keystorepass:{type: string, "propertyOrder": 21, default: "123456"},
												keypass:{type: string, "propertyOrder": 22, default: "123456"},
												truststore:{type: string, "propertyOrder": 23, default: "config\/certificates\/truststore.p12"},
												truststorepass:{type: string, "propertyOrder": 24, default: "123456"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
							required:[eventhandler],
							"additionalProperties": false
						},
						{
							"title": "GATEKEEPER",
							properties:
							{
								name:{ type: string, default: "GATEKEEPER", "propertyOrder": 100, options:{ hidden: true}},
								deployImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},								
								gatekeeper:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								workdir:{type: string, default: "\/arrowhead\/gatekeeper", "propertyOrder": 5},								
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
													"watch":{ "namespace": "root.Docker.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
												gatekeeper_keystore:{type: string, default: "config\/certificates\/gatekeeper.p12"},
												gatekeeper_keystore_pass:{type: string, default: "123456"},
												gatekeeper_keypass:{type: string, default: "123456"},
												cloud_keystore:{ type: string, default: "config\/certificates\/truststore.p12"},
												cloud_keystore_pass:{type: string, default: "123456"},
												cloud_keypass: {type: string, default: "123456"},
												master_arrowhead_cert:{type: string, default: "config\/certificates\/master.crt"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
							required:[gatekeeper],
							"additionalProperties": false
						},
						{
							"title": "ORCHESTRATOR",
							properties:
							{
								name:{ type: string, default: "ORCHESTRATOR", "propertyOrder": 100, options:{ hidden: true}},
								deployImage:{type: string, enum:[one1, one2, one3], default: one1, "propertyOrder": 1},
								orchestrator:{type: boolean, default: true, readOnly: true, format: checkbox, options:{hidden:true}},
								workdir:{type: string, default: "\/arrowhead\/orchestrator", "propertyOrder": 5},								
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
													"watch":{ "namespace": "root.Docker.cloud", db: "root.ArrowHead.DB.arrowHeadDB"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
												keystore:{type: string, "propertyOrder": 20, default: "config\/certificates\/orchestrator.p12"},
												keystorepass:{type: string, "propertyOrder": 21, default: "123456"},
												keypass:{type: string, "propertyOrder": 22, default: "123456"},
												truststore:{type: string, "propertyOrder": 23, default: "config\/certificates\/truststore.p12"},
												truststorepass:{type: string, "propertyOrder": 24, default: "123456"}
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
							required:[orchestrator],
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
								"watch":{ "namespace": "root.Docker.cloud", "db": "root.ArrowHead.DB.arrowHeadLogDB"}
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
								"watch":{ "namespace": "root.Docker.cloud"}
							},
							arrowHeadLogDB: {type: string, default: log, "propertyOrder": 10},
							arrowHeadLogDBAdress:
							{
								type: string, default: "", "propertyOrder": 11, readOnly: true,
								"template": "{{namespace}}-db-server",
								"watch":{ "namespace": "root.Docker.cloud"}
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
					}
				}
			}
		}
	},
	startval:
	{
		Node:
		{
			name: ""
		},
		Docker:
		{
			cloud: "my-cloud",
			delay: 10
		},	
		Images:
		[
			{
				name: DB,
				database: true,
				deployImage: one1,
				initDBScript: true
			},
			{
				name: SERVICE_REGISTRY,
				registry: true, 
				deployImage: "one1",
				workdir: "\/arrowhead\/serviceregistry_sql",
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
				name: AUTHORIZATION,
				authorization:true, 
				deployImage: "one1",
				workdir: "\/arrowhead\/authorization",
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
				name: GATEWAY,
				gateway:true, 
				deployImage: "one1",
				workdir: "\/arrowhead\/gateway",
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
				name: EVENTHANDLER,
				eventhandler:true, 
				deployImage: "one1",
				workdir: "\/arrowhead\/eventhandler",
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
				name: GATEKEEPER,
				gatekeeper: true, 
				deployImage: "one1",
				workdir: "\/arrowhead\/gatekeeper",
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
				name: ORCHESTRATOR,
				orchestrator: true, 
				deployImage: "one1",
				workdir: "\/arrowhead\/orchestrator",
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
				def information = [:]
				
				information.put("kernel", checkNode( aSlave,  "kernel" ) )
				information.put("name", aSlave.name )
				information.put("os", aSlave.getComputer().getOSDescription())
				information.put("networks", checkNode( aSlave,  "docker_networks" ) )
				//~ information.put("containers", checkNode( aSlave,  "containers" ) )
				
				artifacts.add(information)
				
			}
		}
	}
	
	return artifacts
}

def getFolderOwner(name)
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

def checkNode( slave, op)
{
	//uname = 'def proc = "uname -a".execute(); proc.waitFor(); println proc.in.text';
	//println slave.name;
	//~ println RemotingDiagnostics.executeGroovy(print_ip, slave.getChannel());
	//~ println RemotingDiagnostics.executeGroovy(print_hostname, slave.getChannel());
	//~ println RemotingDiagnostics.executeGroovy(uname, slave.getChannel());
	
	def unameCmd = 'def proc = "uname -a".execute(); proc.waitFor(); println proc.in.text';
	def networksCmd = 'def proc = "docker network ls --format {{.Name}}:__:".execute(); proc.waitFor(); println proc.in.text';
	def containersCmd = 'def proc = "docker ps -a --format \'{{.Names}}__is__{{.Status}}\'".execute(); proc.waitFor(); println proc.in.text';
	
	
	if( op.equals("docker_networks") )
	{
		def rawOut = RemotingDiagnostics.executeGroovy(networksCmd, slave.getChannel())
		
		def networkArray = rawOut.split(":__:")
		
		def outNetworks = [:]
		
		if( networkArray )
		{
			networkArray.each
			{
				network ->
					
					//~ println "Network Name; '" + network.trim() + "'"
					
					if( ! ( network.trim() ) )
					{
						//~ println "Network Name; '" + network.trim() + "' skipped"
						return
					}
					
					if( network.trim().equals("bridge") || network.trim().equals("host") || network.trim().equals("none") )
					{
						//~ println "Network Name; '" + network.trim() + "' skipped"
						return
					}
					
					def networkKey = network.trim()
					
					def runCmd = 'def proc = "docker network inspect '+networkKey+'".execute(); proc.waitFor(); println proc.in.text';
					
					def jsonNetwork = RemotingDiagnostics.executeGroovy(runCmd, slave.getChannel())
					
					if( ! jsonNetwork )
						return []
					
					
					
					def networks = Boon.fromJson(jsonNetwork)
					
					outNetworks.put( networks.Name , [:] )
					
					outNetworks[networks.Name].put( "name", networks.Name)
					outNetworks[networks.Name].put( "subnet", networks.IPAM.Config[0].Subnet)
					outNetworks[networks.Name].put( "gateway", networks.IPAM.Config[0].Gateway)

					
					outNetworks[networks.Name].put( "containers", [])
					
					
					
					for( container in networks.Containers )
					{						
						container.each
						{
							key, value ->
								
								//~ println value.Name
								
								def runCmdContainer = 'def proc = "docker container inspect '+value.Name+'".execute(); proc.waitFor(); println proc.in.text';
								
								def containerDetails = RemotingDiagnostics.executeGroovy(runCmdContainer, slave.getChannel())
								
								containerDetails = Boon.fromJson(containerDetails)
								
								def containerStatus = containerDetails[0].State.Status
								def containerRunning =  containerDetails[0].State.Running
								def containerDead = containerDetails[0].State.Dead
								def containerError = containerDetails[0].State.Error
								
								def containerImage = containerDetails[0].Image
								
								def runCmdImage = 'def proc = "docker image inspect '+containerImage+'".execute(); proc.waitFor(); println proc.in.text';
								
								def imageDetails = RemotingDiagnostics.executeGroovy(runCmdImage, slave.getChannel())
								
								imageDetails = Boon.fromJson(imageDetails)
								
								def imageRepoTags = imageDetails[0].RepoTags[0]
															
								outNetworks[networks.Name]["containers"].add(
									[
										name: value.Name, mac: value.MacAddress, ip4: value.IPv4Address,
										image: imageRepoTags, status: containerStatus, running: containerRunning, error: containerError
									]
								)
						}
					}
			}
			
			return outNetworks
		}
	}
	
	if( op.equals("kernel") )
		return RemotingDiagnostics.executeGroovy(unameCmd, slave.getChannel()).trim()
	
	
	
	if( op.equals("containers") )
		return RemotingDiagnostics.executeGroovy(containersCmd, slave.getChannel()).trim()
}


def baseFolderProperties = getCredentials("portus_api_token", "REPO_URL", "PORTUS_USER", base.trim())
def url = baseFolderProperties.folder.REPO_URL
def user = baseFolderProperties.creds.portus_api_token.user
def pass = baseFolderProperties.creds.portus_api_token.pass


def repos = portusApiGET(url, "/api/v1/repositories?all=true", user, pass)

def buildImages = []

def owner = getFolderOwner(base)

def nodes = getNodesForOwner(owner)

repos.each
{
	repo ->
			
		if( repo.namespace.name.startsWith("cpsiot-deploy") )
		{
			def image = repo.namespace.name + "/" + repo.name
			
			def tags = portusApiGET(url, "/api/v1/repositories/"+repo.id+"/tags", user, pass)
			
			tags.each
			{
				tag ->
					
					buildImages.add(image + ":" + tag.name)
			}
		}
		
}

jsonEditorOptions.schema.properties.Images.items.oneOf[0].properties.deployImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[1].properties.deployImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[2].properties.deployImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[3].properties.deployImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[4].properties.deployImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[5].properties.deployImage.enum = buildImages

jsonEditorOptions.schema.properties.Images.items.oneOf[6].properties.deployImage.enum = buildImages



if( nodes )
{
	def startNetwork = ""
	jsonEditorOptions.schema.properties.Node.oneOf = []
	
	nodes.each
	{
		node ->
			
			
			def nodeProperties = 
			[
				"title": node.name,
				"properties":
				[
					"name": ["type": "string", "default": node.name, "readOnly": true],
					"kernel": ["type": "string", "default": node.kernel, "readOnly": true],
					"os": ["type": "string", "default": node.os, "readOnly": true],
					"noNode": ["type": "boolean", "default": false, "readOnly": true, "format": "checkbox", "options":["hidden": true]],
					"networks": [ "type": "object", "oneOf": []]
				],
				"required": ["name"],
				"additionalProperties": false
			]			
			
			node.networks.each
			{
				key, network ->
				
						if( !startNetwork)
							startNetwork = network.name
						
						def net1 = 
						[
							"title": network.name,
							"properties":
							[
								"name": ["type": "string", "default": network.name, "readOnly": true],
								"subnet": ["type": "string", "default": network.subnet, "readOnly": true],
								"gateway": ["type": "string", "default": network.subnet, "readOnly": true]
							]
						]
						
						nodeProperties.properties.networks.oneOf.add(net1)
			}
			
			jsonEditorOptions.schema.properties.Node.oneOf.add(nodeProperties)
	}
	
	jsonEditorOptions.startval.Node = 
	[
		"name": nodes[0].name, "kernel": nodes[0].kernel, "noNode": false,  "os": nodes[0].os ,
		"networks": ["name": nodes[0].networks[startNetwork].name, "subnet": nodes[0].networks[startNetwork].subnet, "gateway": nodes[0].networks[startNetwork].gateway]
	]
}
else
{
	jsonEditorOptions.schema.properties.Node = 
	[
		"type": "object",
		"properties":
		[
			"name": ["type": "string", "default": "No Node Online", "readOnly": true],
			"noNode": ["type": "boolean", "default": true, "readOnly": true, "format": "checkbox", "options":["hidden": true]]
		],
		"additionalProperties": false	
	]
	
	jsonEditorOptions.startval.Node = [ "name": "No Node Online", "noNode": true ]
}

//jsonEditorOptions.startval.Node.name = nodesEnum[0]

jsonEditorOptions.startval.Images[0].deployImage = buildImages[0]
jsonEditorOptions.startval.Images[1].deployImage = buildImages[0]
jsonEditorOptions.startval.Images[2].deployImage = buildImages[0]
jsonEditorOptions.startval.Images[3].deployImage = buildImages[0]
jsonEditorOptions.startval.Images[4].deployImage = buildImages[0]
jsonEditorOptions.startval.Images[5].deployImage = buildImages[0]
jsonEditorOptions.startval.Images[6].deployImage = buildImages[0]

return jsonEditorOptions

/*
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
													"watch":{ "namespace": "root.Docker.cloud"}
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
*/
