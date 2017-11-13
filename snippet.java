
BasicConnection connection = new BasicConnection();
connection.setUrl(url);
connection.setUsername(userName);
connection.setPassword(password);   	      	
DisableSecurity.trustEveryone();
connection.connect();  

VimPortType vimPort = connection.getVimPort();
ServiceContent serviceContent = connection.getServiceContent();
GetMOREF getMOREFs = new GetMOREF(connection);
WaitForValues waitForValues = new WaitForValues(connection);
if(vimPort!=null && serviceContent!=null && getMOREFs!=null && waitForValues!=null)
{ 
ManagedObjectReference vmRef = getMOREFs.inContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(templateName);
if(vmRef == null)
{
System.out.println("The specified Template name : "+templateName+" for cloning is not found...???");
return null;
}
				    
ManagedObjectReference vmFolderRef = null;
if(folderName!=null)
{
vmFolderRef = getMOREFs.inContainerByType(serviceContent.getRootFolder(), "Folder").get(folderName);
if(vmFolderRef==null)
{
System.out.println("Provided folder name \'"+folderName+"\' is not found...??");
return null;
}
}
else
{
vmFolderRef =(ManagedObjectReference) getDynamicProperty(serviceContent.getRootFolder(),"vmFolder", connection);
if (vmFolderRef == null) 
{
System.out.println("Default Datacenter folder is not found...??");
return null;
}
}	
				        
ManagedObjectReference hostRef = getMOREFs.inContainerByType(serviceContent.getRootFolder(), "HostSystem").get(hostName);
if (hostRef == null) 
{
System.out.println("The specified host "+hostName+" is not found...??");
return null;
}
				    
ManagedObjectReference datastoreRef = getMOREFs.inContainerByType(serviceContent.getRootFolder(), "Datastore").get(datastoreName);
if (datastoreRef == null) 
{
System.out.println("The specified datastore "+datastoreName+" is not found...??");
return null;
}

ManagedObjectReference clusterComputeRef = getMOREFs.inContainerByType(serviceContent.getRootFolder(), "ClusterComputeResource").get(clusterName);
if (clusterComputeRef == null) 
{
System.out.println("The specified Cluster "+clusterName+" is not found...??");
return null;
}
				    
ManagedObjectReference resourcePoolRef = null; 
Map<String, Object> resourcePool = getMOREFs.entityProps(clusterComputeRef, new String[]{"resourcePool"});
for(Map.Entry<String,Object> entry : resourcePool.entrySet())
{
resourcePoolRef = (ManagedObjectReference) entry.getValue();
if (resourcePoolRef == null) 
{
System.out.println("The Resource Pool of cluster "+clusterName+" is not found...??");
return null;
}
}			    
				    
VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
relocSpec.setDatastore(datastoreRef);
relocSpec.setHost(hostRef);
relocSpec.setPool(resourcePoolRef);
cloneSpec.setLocation(relocSpec);
cloneSpec.setPowerOn(false);
cloneSpec.setTemplate(false);
				 			    
System.out.println("Cloning Virtual Machine "+vmName+" from template name : "+templateName);
ManagedObjectReference cloneTask = vimPort.cloneVMTask(vmRef, vmFolderRef, vmName, cloneSpec);
				    
if (getTaskResultAfterDone(cloneTask, connection))
{
System.out.println("Successfully cloned Virtual Machine clone name is : "+vmName);
return "success";
} 
else 
{
System.out.println("Failure Cloning Virtual Machine to clone name is "+vmName);			    	
}
}
else
{
System.out.println("Problem while getting vimport/getMors/serviceContent/waitForValues object from connection object.....??");
}