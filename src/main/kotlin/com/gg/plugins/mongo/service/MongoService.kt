/*
 * Copyright (c) 2018 David Boissier.
 * Modifications Copyright (c) 2022 Geetesh Gupta.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gg.plugins.mongo.service

import com.gg.plugins.mongo.config.ConfigurationException
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.config.SshTunnelingConfiguration
import com.gg.plugins.mongo.config.ssh.SshConnection
import com.gg.plugins.mongo.model.*
import com.intellij.openapi.project.Project
import com.mongodb.AuthenticationMechanism
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.MongoException
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.FindOneAndReplaceOptions
import org.apache.commons.lang.StringUtils
import org.bson.Document
import org.bson.types.ObjectId
import java.util.LinkedList
import java.util.TreeSet
import java.util.function.Consumer

class MongoService {
    private val mongoServers: MutableList<MongoServer?> = LinkedList()
    fun connect(configuration: ServerConfiguration) {
        val task: Task = object : Task {
            override fun run(mongoClient: MongoClient) {
                val userDatabase = configuration.userDatabase
                val databaseName = if (userDatabase.isNullOrEmpty()) "test" else userDatabase
                mongoClient.getDatabase(databaseName).listCollectionNames().first()
            }
        }
        executeTask(configuration, task)
    }

    private fun executeTask(configuration: ServerConfiguration, perform: Task) {
        if (SshTunnelingConfiguration.isEmpty(configuration.sshTunnelingConfiguration)) {
            execute(configuration, perform)
        } else {
            SshConnection.create(configuration).use { execute(configuration, perform) }
        }
    }

    private fun execute(configuration: ServerConfiguration, task: Task) {
        try {
            createMongoClient(configuration).use { mongoClient -> task.run(mongoClient) }
        } catch (ex: MongoException) {
            throw ConfigurationException(ex)
        }
    }

    private fun createMongoClient(configuration: ServerConfiguration): MongoClient {
        val serverUrls = configuration.serverUrls
        if (serverUrls.isEmpty()) {
            throw ConfigurationException("Server host is not set")
        }
        val serverAddresses: MutableList<ServerAddress> = LinkedList()
        if (SshTunnelingConfiguration.isEmpty(configuration.sshTunnelingConfiguration)) {
            for (serverUrl in serverUrls) {
                val hostAndPort: ServerConfiguration.HostAndPort = ServerConfiguration.extractHostAndPort(
                    serverUrl
                )
                serverAddresses.add(ServerAddress(hostAndPort.host, hostAndPort.port))
            }
        } else {
            serverAddresses.add(ServerAddress(DEFAULT_TUNNEL_LOCAL_HOST, DEFAULT_TUNNEL_LOCAL_PORT))
        }
        val options = MongoClientOptions.builder()
            .sslEnabled(configuration.isSslConnection)
            .readPreference(configuration.readPreference)
            .codecRegistry(MongoClient.getDefaultCodecRegistry())
            .build()
        return if (StringUtils.isEmpty(configuration.username)) {
            MongoClient(serverAddresses, options)
        } else {
            val credential = getMongoCredential(configuration)
            MongoClient(serverAddresses, credential, options)
        }
    }

    private fun getMongoCredential(configuration: ServerConfiguration): MongoCredential {
        val authenticationMechanism = configuration.authenticationMechanism
        if (authenticationMechanism == null) {
            return MongoCredential.createPlainCredential(
                configuration.username!!,
                getAuthenticationDatabase(configuration),
                configuration.password!!.toCharArray()
            )
        } else {
            if (AuthenticationMechanism.SCRAM_SHA_1 == authenticationMechanism) {
                return MongoCredential.createScramSha1Credential(
                    configuration.username!!,
                    getAuthenticationDatabase(configuration),
                    configuration.password!!.toCharArray()
                )
            }
        }
        throw IllegalArgumentException("Unsupported authentication mechanism: $authenticationMechanism")
    }

    fun cleanUpServers() {
        mongoServers.clear()
    }

    fun registerServer(mongoServer: MongoServer?) {
        mongoServers.add(mongoServer)
    }

    val servers: List<MongoServer?>
        get() = mongoServers

    fun loadDatabases(mongoServer: MongoServer, configuration: ServerConfiguration): MutableSet<MongoDatabase> {
        val mongoDatabases: MutableSet<MongoDatabase> = TreeSet()
        val perform: TaskWithReturnedObject<MutableSet<MongoDatabase>> =
            object : TaskWithReturnedObject<MutableSet<MongoDatabase>> {
                override fun run(mongoClient: MongoClient): MutableSet<MongoDatabase> {
                    val userDatabase = configuration.userDatabase
                    when {
                        userDatabase.isNullOrEmpty() -> {
                            val databaseNames = mongoClient.listDatabaseNames()
                            for (databaseName in databaseNames) {
                                val database = mongoClient.getDatabase(databaseName)
                                val mongoDatabase = MongoDatabase(database.name, mongoServer)
                                mongoDatabases.add(mongoDatabase)
                            }
                        }

                        else -> {
                            val database = mongoClient.getDatabase(userDatabase)
                            val mongoDatabase = MongoDatabase(database.name, mongoServer)
                            mongoDatabases.add(mongoDatabase)
                        }
                    }
                    return mongoDatabases
                }
            }
        return executeTask(configuration, perform)
    }

    private fun <T> executeTask(configuration: ServerConfiguration, perform: TaskWithReturnedObject<T>): T {
        if (SshTunnelingConfiguration.isEmpty(configuration.sshTunnelingConfiguration)) {
            return execute(configuration, perform)
        } else {
            SshConnection.create(configuration).use { return execute(configuration, perform) }
        }
    }

    private fun <T> execute(configuration: ServerConfiguration, perform: TaskWithReturnedObject<T>): T {
        try {
            createMongoClient(configuration).use { mongo -> return perform.run(mongo) }
        } catch (mongoEx: MongoException) {
            throw ConfigurationException(mongoEx)
        }
    }

    fun loadCollections(
        mongoDatabase: MongoDatabase,
        configuration: ServerConfiguration
    ): Set<com.gg.plugins.mongo.model.MongoCollection> {
        val collections: MutableSet<com.gg.plugins.mongo.model.MongoCollection> = TreeSet()
        val perform: TaskWithReturnedObject<Set<com.gg.plugins.mongo.model.MongoCollection>> =
            object : TaskWithReturnedObject<Set<com.gg.plugins.mongo.model.MongoCollection>> {
                override fun run(mongoClient: MongoClient): Set<com.gg.plugins.mongo.model.MongoCollection> {
                    val database = mongoClient.getDatabase(mongoDatabase.name)
                    val collectionNames = database.listCollectionNames()
                    for (collectionName in collectionNames) {
                        collections.add(MongoCollection(collectionName, mongoDatabase))
                    }
                    return collections
                }
            }
        return executeTask(configuration, perform)
    }

    fun findMongoDocuments(
        configuration: ServerConfiguration,
        mongoCollection: com.gg.plugins.mongo.model.MongoCollection,
        mongoQueryOptions: MongoQueryOptions?
    ): MongoCollectionResult {
        val task: TaskWithReturnedObject<MongoCollectionResult> =
            object : TaskWithReturnedObject<MongoCollectionResult> {
                override fun run(mongoClient: MongoClient): MongoCollectionResult {
                    val mongoDatabase = mongoCollection.parentDatabase
                    val database = mongoClient.getDatabase(mongoDatabase.name)
                    val collection = database.getCollection(mongoCollection.name)
                    val mongoCollectionResult = MongoCollectionResult(mongoCollection.name)
                    if (mongoQueryOptions!!.isAggregate()) {
                        return aggregate(mongoQueryOptions, mongoCollectionResult, collection)
                    }
                    return find(mongoQueryOptions, mongoCollectionResult, collection)
                }
            }
        return executeTask(configuration, task)
    }

    private fun aggregate(
        mongoQueryOptions: MongoQueryOptions,
        mongoCollectionResult: MongoCollectionResult,
        collection: MongoCollection<Document>
    ): MongoCollectionResult {
        val aggregate = collection.aggregate(mongoQueryOptions.operations)
        val index = 0
        val iterator: Iterator<Document> = aggregate.iterator()
        while (iterator.hasNext() && index < mongoQueryOptions.resultLimit) {
            mongoCollectionResult.add(iterator.next())
        }
        return mongoCollectionResult
    }

    private fun find(
        mongoQueryOptions: MongoQueryOptions,
        mongoCollectionResult: MongoCollectionResult,
        collection: MongoCollection<Document>
    ): MongoCollectionResult {
        val filter = mongoQueryOptions.filter
        val projection = mongoQueryOptions.projection
        val sort = mongoQueryOptions.sort
        val cursor = collection.find(filter)
        if (MongoQueryOptions.EMPTY_DOCUMENT != projection) {
            cursor.projection(projection)
        }
        if (MongoQueryOptions.EMPTY_DOCUMENT != sort) {
            cursor.sort(sort)
        }
        val resultLimit = mongoQueryOptions.resultLimit
        if (resultLimit > 0) {
            cursor.limit(resultLimit)
        }
        cursor.forEach(Consumer { document: Document -> mongoCollectionResult.add(document) } as Consumer<in Document>)
        return mongoCollectionResult
    }

    fun findMongoDocument(
        configuration: ServerConfiguration,
        mongoCollection: com.gg.plugins.mongo.model.MongoCollection,
        _id: Any
    ): Document? {
        val task: TaskWithReturnedObject<Document?> = object : TaskWithReturnedObject<Document?> {
            override fun run(mongoClient: MongoClient): Document? {
                val mongoDatabase = mongoCollection.parentDatabase
                val database = mongoClient.getDatabase(mongoDatabase.name)
                val collection = database.getCollection(mongoCollection.name)
                val foundDocuments = collection.find(BasicDBObject("_id", _id))
                return foundDocuments.first()
            }
        }
        return executeTask(configuration, task)
    }

    fun update(
        configuration: ServerConfiguration,
        mongoCollection: com.gg.plugins.mongo.model.MongoCollection,
        mongoDocument: Document?
    ) {
        val task = object : Task {
            override fun run(mongoClient: MongoClient) {
                val mongoDatabase = mongoCollection.parentDatabase
                val database = mongoClient.getDatabase(mongoDatabase.name)
                val collection = database.getCollection(mongoCollection.name)
                if (!mongoDocument!!.containsKey("_id")) {
                    collection.insertOne(mongoDocument)
                } else {
                    collection.findOneAndReplace(
                        Document("_id", mongoDocument["_id"]),
                        mongoDocument,
                        FindOneAndReplaceOptions().upsert(true)
                    )
                }
            }
        }
        executeTask(configuration, task)
    }

    fun delete(
        configuration: ServerConfiguration,
        mongoCollection: com.gg.plugins.mongo.model.MongoCollection,
        _id: ObjectId
    ) {
        val task = object : Task {
            override fun run(mongoClient: MongoClient) {
                val mongoDatabase: MongoDatabase = mongoCollection.parentDatabase
                val database = mongoClient.getDatabase(mongoDatabase.name)
                val collection = database.getCollection(mongoCollection.name)

                collection.deleteOne(Document("_id", _id))
            }
        }
        executeTask(configuration, task)
    }

    fun removeCollection(
        configuration: ServerConfiguration,
        mongoCollection: com.gg.plugins.mongo.model.MongoCollection
    ) {
        val task = object : Task {
            override fun run(mongoClient: MongoClient) {
                val mongoDatabase = mongoCollection.parentDatabase
                val database = mongoClient.getDatabase(mongoDatabase.name)
                val collection = database.getCollection(mongoCollection.name)
                collection.drop()
            }
        }
        executeTask(configuration, task)
    }

    fun removeDatabase(configuration: ServerConfiguration, selectedDatabase: MongoDatabase) {
        val task: Task = object : Task {
            override fun run(mongoClient: MongoClient) {
                mongoClient.dropDatabase(selectedDatabase.name)
            }
        }
        executeTask(configuration, task)
    }

    fun importData(
        configuration: ServerConfiguration,
        mongoCollection: com.gg.plugins.mongo.model.MongoCollection,
        mongoDocuments: List<Document>,
        replaceAllDocuments: Boolean
    ) {
        val task = object : Task {
            override fun run(mongoClient: MongoClient) {
                val mongoDatabase = mongoCollection.parentDatabase
                val database = mongoClient.getDatabase(mongoDatabase.name)
                val collection = database.getCollection(mongoCollection.name)
                if (replaceAllDocuments) {
                    collection.deleteMany(EMPTY_DOC)
                }
                collection.insertMany(mongoDocuments)
            }
        }
        executeTask(configuration, task)
    }

    private interface Task {
        fun run(mongoClient: MongoClient)
    }

    private interface TaskWithReturnedObject<T> {
        fun run(mongoClient: MongoClient): T
    }

    companion object {
        private const val DEFAULT_AUTHENTICATION_DATABASE = "admin"
        private const val DEFAULT_TUNNEL_LOCAL_HOST = "localhost"
        private const val DEFAULT_TUNNEL_LOCAL_PORT = 9080
        private val EMPTY_DOC = Document()
        fun getInstance(project: Project): MongoService {
            return project.getService(MongoService::class.java)
        }

        private fun getAuthenticationDatabase(configuration: ServerConfiguration): String {
            val authenticationDatabase = configuration.authenticationDatabase
            return if (authenticationDatabase.isNullOrEmpty()) DEFAULT_AUTHENTICATION_DATABASE else authenticationDatabase
        }
    }
}