package scc.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.UniqueKey;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithConsistencyPolicy;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithCreate;
import com.microsoft.azure.management.cosmosdb.KeyKind;
import com.microsoft.azure.management.redis.RedisAccessKeys;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisKeyType;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.storage.AccessTier;
import com.microsoft.azure.management.storage.BlobContainer;
import com.microsoft.azure.management.storage.PublicAccess;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.StorageAccountSkuType;
import com.microsoft.rest.LogLevel;

public class AzureManagement {
	// Auth file location
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
	// TODO: This file should be created by running in the console:
	// az ad sp create-for-rbac --sdk-auth > azure.auth
	static final String AZURE_AUTH_LOCATION = "azure.auth";

	private static final int ENTITY_TTL = -1; // (never expire by default)
	private static final int CALENDAR_TTL = -1; // (never expire by default)
	private static final int FORUM_TTL = -1; // (never expire by default)
	private static final int FORUM_MESSAGE_TTL = 90 * 60 * 60 * 24; // expire all documents after 90 days
	private static final int PERIOD_TTL = 90 * 60 * 60 * 24; // expire all documents after 90 days
	private static final int RESERVATION_TTL = 90 * 60 * 60 * 24; // expire all documents after 90 days

	public static Azure createManagementClient(String authFile) throws CloudException, IOException {
		File credFile = new File(authFile);
		Azure azure = Azure.configure().withLogLevel(LogLevel.BASIC).authenticate(credFile).withDefaultSubscription();
		System.out.println("Azure client created with success");
		return azure;
	}

	public static ResourceGroup createResourceGroup(Azure azure, String rgName, Region region) {
		ResourceGroup resourceGroup = azure.resourceGroups().define(rgName).withRegion(region).create();
		return resourceGroup;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Azure Storage Account CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static StorageAccount createStorageAccount(Azure azure, String rgName, String name, Region region) {
		StorageAccount storageAccount = azure.storageAccounts().define(name).withRegion(region)
				.withNewResourceGroup(rgName).withGeneralPurposeAccountKindV2().withAccessFromAllNetworks()
				.withBlobStorageAccountKind().withAccessTier(AccessTier.HOT)
				.withSku(StorageAccountSkuType.STANDARD_RAGRS).create();
		System.out.println("Storage account created with success: name = " + name + " ; group = " + rgName
				+ " ; region = " + region.name());
		return storageAccount;
	}

	private static BlobContainer createBlobContainer(Azure azure, String rgName, String accountName,
			String containerName) {
		BlobContainer container = azure.storageBlobContainers().defineContainer(containerName)
				.withExistingBlobService(rgName, accountName).withPublicAccess(PublicAccess.BLOB).create();
		System.out.println("Blob container created with success: name = " + containerName + " ; group = " + rgName
				+ " ; account = " + accountName);
		return container;
	}

	public synchronized static void recordStorageKey(Azure azure, String propFilename, String settingsFilename,
			String functionsName, String functionsRGName, StorageAccount account) throws IOException {
	}

	public synchronized static void dumpStorageKey(Map<String, String> props, String propFilename,
			String settingsFilename, String appName, String functionName, String rgName, StorageAccount account)
			throws IOException {
		List<StorageAccountKey> storageAccountKeys = account.getKeys();
		storageAccountKeys = account.regenerateKey(storageAccountKeys.get(0).keyName());

		StringBuffer keyB = new StringBuffer();
		keyB.append("DefaultEndpointsProtocol=https;AccountName=");
		keyB.append(account.name());
		keyB.append(";AccountKey=");
		keyB.append(storageAccountKeys.get(0).value());
		keyB.append(";EndpointSuffix=core.windows.net");
		String key = keyB.toString();

		synchronized (props) {
			props.put("BlobStoreConnection", key);

		}

		synchronized (AzureManagement.class) {
			Files.write(Paths.get(propFilename), ("BlobStoreConnection=" + key + "\n").getBytes(),
					StandardOpenOption.APPEND);
		}
		StringBuffer cmd = new StringBuffer();
		if (functionName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(functionName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"BlobStoreConnection=");
			cmd.append(key);
			cmd.append("\"\n");
		}
		if (appName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"BlobStoreConnection=");
			cmd.append(key);
			cmd.append("\"\n");
		}
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(settingsFilename), cmd.toString().getBytes(), StandardOpenOption.APPEND);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// COSMOS DB CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static CosmosDBAccount createCosmosDBAccount(Azure azure, String rgName, String name, Region[] regions) {
		WithConsistencyPolicy step = azure.cosmosDBAccounts().define(name).withRegion(regions[0])
				.withExistingResourceGroup(rgName).withDataModelSql();
		CosmosDBAccount account = null;
		if (regions.length == 1) {
			account = step.withSessionConsistency().withWriteReplication(regions[0]).create();
		} else {
			WithCreate create = step.withSessionConsistency().withWriteReplication(regions[0])
					.withMultipleWriteLocationsEnabled(true);
			for (int i = 1; i < regions.length; i++) {
				create = create.withSessionConsistency().withWriteReplication(regions[i]);
			}
			account = create.create();
		}
		account.regenerateKey(KeyKind.PRIMARY);
		System.out.println("CosmosDB account created with success: name = " + name + " ; group = " + rgName
				+ " ; main region = " + regions[0].name() + " ; number regions = " + regions.length);
		return account;
	}

	public synchronized static void dumpCosmosDBKey(Map<String, String> props, String propFilename,
			String settingsFilename, String appName, String functionName, String rgName, String databaseName,
			CosmosDBAccount account) throws IOException {
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(propFilename),
					("COSMOSDB_KEY=" + account.listKeys().primaryMasterKey() + "\n").getBytes(),
					StandardOpenOption.APPEND);
			Files.write(Paths.get(propFilename), ("COSMOSDB_URL=" + account.documentEndpoint() + "\n").getBytes(),
					StandardOpenOption.APPEND);
			Files.write(Paths.get(propFilename), ("COSMOSDB_DATABASE=" + databaseName + "\n").getBytes(),
					StandardOpenOption.APPEND);
		}
		synchronized (props) {
			props.put("COSMOSDB_KEY", account.listKeys().primaryMasterKey());
			props.put("COSMOSDB_URL", account.documentEndpoint());
			props.put("COSMOSDB_DATABASE", databaseName);
		}

		StringBuffer cmd = new StringBuffer();
		if (appName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"COSMOSDB_KEY=");
			cmd.append(account.listKeys().primaryMasterKey());
			cmd.append("\"\n");
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"COSMOSDB_URL=");
			cmd.append(account.documentEndpoint());
			cmd.append("\"\n");
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"COSMOSDB_DATABASE=");
			cmd.append(databaseName);
			cmd.append("\"\n");
		}
		if (functionName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(functionName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"AzureCosmosDBConnection=AccountEndpoint=");
			cmd.append(account.documentEndpoint());
			cmd.append(";AccountKey=");
			cmd.append(account.listKeys().primaryMasterKey());
			cmd.append(";\"");
		}
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(settingsFilename), cmd.toString().getBytes(), StandardOpenOption.APPEND);
		}
	}

	public static CosmosClient getCosmosClient(CosmosDBAccount account) {
		CosmosClient client = new CosmosClientBuilder().endpoint(account.documentEndpoint())
				.key(account.listKeys().primaryMasterKey()).directMode() // comment this is not to use direct mode
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		System.out.println("CosmosDB client created with success: name = " + account.name());
		return client;
	}

	static void createCosmosDatabase(CosmosClient client, String dbname) {
		// create database if not exists
		CosmosDatabaseProperties props = new CosmosDatabaseProperties(dbname);
		ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);
		client.createDatabase(props, throughputProperties);
		System.out.println("CosmosDB database created with success: name = " + dbname);
	}

	static void createCosmosCollection(CosmosClient client, String dbname, String collectionName, String partKeys,
			String[] uniqueKeys, int timeToLive) {
		try {
			CosmosDatabase db = client.getDatabase(dbname);
			CosmosContainerProperties props = new CosmosContainerProperties(collectionName, partKeys);
			if (uniqueKeys != null) {
				UniqueKeyPolicy uniqueKeyDef = new UniqueKeyPolicy();
				List<UniqueKey> uniqueKeyL = new ArrayList<UniqueKey>();
				for (String k : uniqueKeys) {
					uniqueKeyL.add(new UniqueKey(Arrays.asList(k)));
				}
				uniqueKeyDef.setUniqueKeys(uniqueKeyL);
				props.setUniqueKeyPolicy(uniqueKeyDef);
			}
			// Defines the timeToLive of records of the container
			props.setDefaultTimeToLiveInSeconds(timeToLive);
			db.createContainer(props);
			System.out.println("CosmosDB collection created with success: name = " + collectionName + "@" + dbname);

		} catch (Exception e) { // TODO: Something has gone terribly wrong.
			e.printStackTrace();
			return;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// REDIS CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public static RedisCache createRedis(Azure azure, String rgName, String name, Region region) {
		try {
			Creatable<RedisCache> redisCacheDefinition = azure.redisCaches().define(name).withRegion(region)
					.withNewResourceGroup(rgName).withBasicSku(0);

			return azure.redisCaches().create(redisCacheDefinition).get(redisCacheDefinition.key());
		} finally {
			System.out.println("Redis cache created with success: name = " + name + "@" + region);
		}
	}

	public synchronized static void dumpRedisCacheInfo(Map<String, String> props, String propFilename, RedisCache cache)
			throws IOException {
		RedisAccessKeys redisAccessKey = cache.regenerateKey(RedisKeyType.PRIMARY);
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(propFilename), ("REDIS_KEY=" + redisAccessKey.primaryKey() + "\n").getBytes(),
					StandardOpenOption.APPEND);
			Files.write(Paths.get(propFilename), ("REDIS_URL=" + cache.hostName() + "\n").getBytes(),
					StandardOpenOption.APPEND);
		}
		synchronized (props) {
			props.put("REDIS_KEY", redisAccessKey.primaryKey());
			props.put("REDIS_URL", cache.hostName());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// AZURE DELETE CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void deleteResourceGroup(Azure azure, String rgName) {
		azure.resourceGroups().deleteByName(rgName);
	}

	public static void main(String[] args) {
		try {
			// TODO: These variable allow you to control what is being created
			final boolean CREATE_STORAGE = false;
			final boolean CREATE_COSMOSDB = false;
			final boolean CREATE_REDIS = AdvanceFeatures.getProperty("Redis");
			final boolean CREATE_FUNCTIONS = AdvanceFeatures.getProperty("Functions");
			final boolean CREATE_SEARCH = AdvanceFeatures.getProperty("Search");
			final boolean CREATE_GEO_REPLICATION = AdvanceFeatures.getProperty("Geo-Replicated");

			// TODO: change your suffix and other names if you want
			final String MY_SUFFIX = "41812-42764-50092"; // Add your suffix here
			final String AZURE_COSMOSDB_NAME = "scc-groupBD" + MY_SUFFIX; // Cosmos DB account name
			final String AZURE_COSMOSDB_DATABASE = "sccBD" + MY_SUFFIX; // Cosmos DB database name
			final String[] BLOB_CONTAINERS = { "images" }; // Contaienrs to add to the blob storage

			// Define the regions to deploy resources here
			// Region.findByLabelOrName(labelOrName)
			final Region[] REGIONS = new Region[] { Region.EUROPE_WEST };

			// Name of property file with keys and URLS to access resources
			final String[] AZURE_PROPS_LOCATIONS = Arrays.stream(REGIONS)
					.map(reg -> "azurekeys-" + reg.name() + ".props").toArray(String[]::new);
			// Name of shell script file with commands to set application setting for you
			// application server
			// and Azure functions
			final String[] AZURE_SETTINGS_LOCATIONS = Arrays.stream(REGIONS)
					.map(reg -> "azureprops-" + reg.name() + ".sh").toArray(String[]::new);
			// Name of resoruce group for each region
			final String[] AZURE_RG_REGIONS = Arrays.stream(REGIONS)
					.map(reg -> "scc2021-rg-" + reg.name() + "-" + MY_SUFFIX).toArray(String[]::new);
			// Name of application server to be launched in each regions -- launching the
			// application
			// server must be done using mvn, as you have been doing
			final String[] AZURE_APP_NAME = Arrays.stream(REGIONS).map(reg -> "sccapp" + reg.name() + MY_SUFFIX)
					.toArray(String[]::new);
			// Name of Azure functions to be launched in each regions
			final String[] AZURE_FUNCTIONS_NAME = Arrays.stream(REGIONS).map(reg -> "sccfun" + reg.name() + MY_SUFFIX)
					.toArray(String[]::new);
			// Name of Blob storage account
			final String[] AZURE_STORAGE_NAME = Arrays.stream(REGIONS).map(reg -> "sccstore" + reg.name() + MY_SUFFIX)
					.toArray(String[]::new);
			// Name of Redis server to be defined
			final String[] AZURE_REDIS_NAME = Arrays.stream(REGIONS).map(reg -> "redis" + reg.name() + MY_SUFFIX)
					.toArray(String[]::new);

			final Map<String, Map<String, String>> props = new HashMap<String, Map<String, String>>();
			Arrays.stream(REGIONS).forEach(reg -> props.put(reg.name(), new HashMap<String, String>()));

			List<Thread> threads = new ArrayList<Thread>();

			final Azure azure = createManagementClient(AZURE_AUTH_LOCATION);
			if (args.length == 1 && args[0].equalsIgnoreCase("-delete")) {
				Arrays.stream(AZURE_RG_REGIONS).forEach(reg -> deleteResourceGroup(azure, reg));
			} else {
				// Init properties files
				for (String propF : AZURE_PROPS_LOCATIONS) {
					Files.deleteIfExists(Paths.get(propF));
					Files.write(Paths.get(propF),
							("# Date : " + new SimpleDateFormat().format(new Date()) + "\n").getBytes(),
							StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				}
				// Init settings files
				for (String propF : AZURE_SETTINGS_LOCATIONS) {
					Files.deleteIfExists(Paths.get(propF));
					Files.write(Paths.get(propF), "".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				}

				// Create resource groups
				for (int i = 0; i < REGIONS.length; i++)
					createResourceGroup(azure, AZURE_RG_REGIONS[i], REGIONS[i]);

				if (CREATE_STORAGE) {
					Thread th = new Thread(() -> {
						try {
							final Azure azure0 = createManagementClient(AZURE_AUTH_LOCATION);
							for (int i = 0; i < REGIONS.length; i++) {
								StorageAccount accountStorage = createStorageAccount(azure0, AZURE_RG_REGIONS[i],
										AZURE_STORAGE_NAME[i], REGIONS[i]);
								dumpStorageKey(props.get(REGIONS[i].name()), AZURE_PROPS_LOCATIONS[i],
										AZURE_SETTINGS_LOCATIONS[i], AZURE_APP_NAME[i], AZURE_FUNCTIONS_NAME[i],
										AZURE_RG_REGIONS[i], accountStorage);
								for (String cont : BLOB_CONTAINERS)
									createBlobContainer(azure0, AZURE_RG_REGIONS[i], AZURE_STORAGE_NAME[i], cont);
							}
						} catch (Exception e) {
							System.err.println("Error while creating storage resources");
							e.printStackTrace();
						}
						return;
					});
					th.start();
					threads.add(th);
				}

				if (CREATE_COSMOSDB) {
					Thread th = new Thread(() -> {
						try {
							final Azure azure0 = createManagementClient(AZURE_AUTH_LOCATION);
							CosmosDBAccount accountCosmosDB = createCosmosDBAccount(azure0, AZURE_RG_REGIONS[0],
									AZURE_COSMOSDB_NAME, REGIONS);
							for (int i = 0; i < REGIONS.length; i++) {
								dumpCosmosDBKey(props.get(REGIONS[i].name()), AZURE_PROPS_LOCATIONS[i],
										AZURE_SETTINGS_LOCATIONS[i], AZURE_APP_NAME[i], AZURE_FUNCTIONS_NAME[i],
										AZURE_RG_REGIONS[i], AZURE_COSMOSDB_DATABASE, accountCosmosDB);
							}
							CosmosClient cosmosClient = getCosmosClient(accountCosmosDB);
							createCosmosDatabase(cosmosClient, AZURE_COSMOSDB_DATABASE);
							// TODO: create the collections you have in your application

							createCosmosCollection(cosmosClient, AZURE_COSMOSDB_DATABASE, TableName.ENTITY.getName(),
									"/id", new String[] { "/id2" }, ENTITY_TTL);
							createCosmosCollection(cosmosClient, AZURE_COSMOSDB_DATABASE, TableName.FORUM.getName(),
									"/id", null, FORUM_TTL);
							createCosmosCollection(cosmosClient, AZURE_COSMOSDB_DATABASE, TableName.CALENDAR.getName(),
									"/id", null, CALENDAR_TTL);
							createCosmosCollection(cosmosClient, AZURE_COSMOSDB_DATABASE,
									TableName.FORUMMESSAGE.getName(), "/id", null, FORUM_MESSAGE_TTL);
							createCosmosCollection(cosmosClient, AZURE_COSMOSDB_DATABASE, TableName.PERIOD.getName(),
									"/id", null, PERIOD_TTL);
							createCosmosCollection(cosmosClient, AZURE_COSMOSDB_DATABASE,
									TableName.RESERVATION.getName(), "/id", null, RESERVATION_TTL);

						} catch (Exception e) {
							System.err.println("Error while creating cosmos db resources");
							e.printStackTrace();
						}
					});
					th.start();
					threads.add(th);
				}

				if (CREATE_REDIS) {
					Thread th = new Thread(() -> {
						try {
							final Azure azure0 = createManagementClient(AZURE_AUTH_LOCATION);
							for (int i = 0; i < REGIONS.length; i++) {
								RedisCache cache = createRedis(azure0, AZURE_RG_REGIONS[i], AZURE_REDIS_NAME[i],
										REGIONS[i]);
								dumpRedisCacheInfo(props.get(REGIONS[i].name()), AZURE_PROPS_LOCATIONS[i], cache);
							}
						} catch (Exception e) {
							System.err.println("Error while creating redis resources");
							e.printStackTrace();
						}
					});
					th.start();
					threads.add(th);
				}

			}
			for (Thread th : threads) {
				th.join();
			}
		} catch (Exception e) {
			System.err.println("Error while creating resources");
			e.printStackTrace();
		}
		System.exit(0);
	}
}
