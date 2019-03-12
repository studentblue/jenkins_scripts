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
	template: "mustache",
	"schema":
	{
		"title": null,
		"type": "object",
		"properties":
		{
			"DockerHub":
			{
				"format": "grid",
				"title": "Docker Hub Repo and Tag",
				"type": "object",
				"additionalProperties": false,
				"propertyOrder": 2,
				"properties":
				{
					"name":
					{
						"type": "string"
					},
					"tag":
					{
						"type": "string"
					}
			    },
			},
			"PortusNameSpace":
			{
				"format": "grid",
				"title": "Portus Name Space",
				"type":"object",
				"additionalProperties": false,
				"propertyOrder": 6,
				"properties":
				{
					"name":
					{
						"propertyOrder" : 1,
						"oneOf":
						[
							{
								"title":"Choose Portus existing Name-Spaces",
								"type":"string",
								"enum":
								[
									"cpsiot_build_linux_v7_arm",
									"test-post_repo",
									"demo-share",
									"testa"
								],
								"options":
								{
									"enum_titles":
										[
											"cpsiot_build_linux_v7_arm",
											"test-post_repo",
											"demo-share",
											"testa"
										]
								}
							},
							{
								"title": "Create New Portus Name-Space",
								"type": "string",
								"default" : ""
							}
						]
					},
					"description":
					{
						"propertyOrder" : 3,
						"type": "string",
						"default" : ""
					},
					"standard":
					{
						"propertyOrder" : 2,
						"type": "boolean",
						"format": "checkbox"
					}
				}
			},
			"PortusTeam":
			{
				"title": "Portus Team",
				"type":"object",
				"additionalProperties": false,
				"propertyOrder": 8,
				"properties":
				{
					"name":
					{
					
						"oneOf":
						[
							{
								"type": "string",
								"title":"Portus existing Teams",
								"type":"string",
								"enum":
								[
									"Team1",
									"Team2",
									"Team3"
								],
								"options":
								{
									"enum_titles":
										[
											"Team1",
											"Team2",
											"Team3"
										]
								}
							},
							{
								"title": "Create New Team",
								"type": "string"
							}
						]
					}
				}
			},
			"PortusImage":
			{
				"title": "Portus Image",
				"type":"object",
				"additionalProperties": false,
				"propertyOrder": 10,
				"properties":
				{
					"name":
					{
						"type": "string",
						"title":"Push as Image-Name"
					},
					"standard":
					{
						"type": "boolean",
						"format": "checkbox"
					}					
				}
			}
		}
	},
	startval:
	{
		"DockerHub":
		{
			"name": "",
			"tag": ""
		},
		"PortusNameSpace":
		{
			"name": "",
			"standard": true,
			"description": ""
		},
		"PortusTeam":
		{
			"name": ""
		},
		"PortusImage":
		{
			"name": "",
			"standard": true
		}
	}
}/);

/*
"template": "{{#name}}0{{\/name}}{{^name}}1{{\/name}}",
						"watch":
						{
							"name": "root.PortusNameSpace.PortusNameSpace"
						}
						"template": "{{#name}}false{{\/name}}{{^name}}true{{\/name}}",
						"watch":
						{
							"name": "root.PortusNameSpace.PortusNameSpace"
						}

*/

