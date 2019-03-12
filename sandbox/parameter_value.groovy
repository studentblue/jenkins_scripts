import groovy.json.JsonSlurper

def recurse 
def versionArraySort = 
{
	a1, a2 ->
		def headCompare = a1[0] <=> a2[0] 
		if (a1.size() == 1 || a2.size() == 1 || headCompare != 0)
		{ 
			return headCompare
		}
		else
		{ 
			return recurse(a1[1..-1], a2[1..-1])
		}
}

// fool Groovy to understand recursive closure 
recurse = versionArraySort

def versionStringSort = {
	s1, s2 ->
		def nums = 
		{
			it.tokenize('.').collect{ it.toInteger() }
		} 
		versionArraySort(nums(s1), nums(s2)) 
}

try
{

	List<String> artifacts = new ArrayList<String>()
	def artifactsUrl = "https://docker-registry-cpsiot-2018.pii.at/api/v1/namespaces"
	
    def artifactsObjectRaw = [
		"curl", "-s",
		"--header", "Accept: application/json", "--header", "Portus-Auth: demo-user:P1yFWtkBNyM4_C5wH212", 
		"--url", "${artifactsUrl}"
	].execute().text
    def jsonSlurper = new JsonSlurper()
    def artifactsJsonObject = jsonSlurper.parseText(artifactsObjectRaw)
    def dataArray = artifactsJsonObject
    artifacts.add("none")
    for(item in dataArray)
    {
		artifacts.add(item.id)
	} 
    //return artifacts.sort(versionStringSort).reverse()
    return artifacts


}
catch (Exception e)
{
	print "There was a problem fetching the artifacts" + e
}
