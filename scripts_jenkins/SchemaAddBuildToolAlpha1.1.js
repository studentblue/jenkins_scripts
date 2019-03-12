editor.on('ready',function()
{
	Q("input[name='root[DockerHub][repo]']").on("keyup",
		function()
		{
			//~ editor.validate();
			console.log("test")
		}
	);
	// Now the api methods will be available
	
});
