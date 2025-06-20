//
// Copyright (c) 2020 Couchbase. All rights reserved.
// COUCHBASE CONFIDENTIAL - part of Couchbase Lite Enterprise Edition
//
package com.couchbase.lite

import com.couchbase.lite.internal.replicator.AbstractCBLWebSocket
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.URI


class ConfigFactoryTest : BaseDbTest() {
    private val testAuthenticator = SessionAuthenticator("mysessionId")
    private val testHeaders = mapOf(AbstractCBLWebSocket.HEADER_COOKIES to "region=nw; city=sf")
    private val testChannels = listOf("channel1", "channel2")
    private val testResolver = DefaultConflictResolver()
    private val testDocIds = listOf("doc1", "doc2")
    private val testPushFilter = ReplicationFilter { _, _ -> ; true }
    private val testPullFilter = ReplicationFilter { _, _ -> ; true }

    private var testEndpoint = URLEndpoint(URI("ws://foo.couchbase.com/db"))
    private lateinit var testPath: String

    @Before
    fun setUpEEConfigFactoryTest() {
        testPath = getScratchDirectoryPath(getUniqueName("confgFactoryTest"))
    }

    ///// Database Configuration

    @Test
    fun testDatabaseConfigurationFactoryDefaults() {
        val config = DatabaseConfigurationFactory.newConfig()
        Assert.assertEquals(Defaults.Database.FULL_SYNC, config.isFullSync)
    }

    @Test
    fun testDatabaseConfigurationFactory() {
        val config = DatabaseConfigurationFactory.newConfig(
            databasePath = testPath,
            fullSync = true
        )
        Assert.assertEquals(testPath, config.directory)
        Assert.assertTrue(config.isFullSync)
    }

    @Test
    fun testDatabaseConfigurationFactoryCopyWithChanges() {
        val config1 = DatabaseConfigurationFactory.newConfig(
            databasePath = testPath,
            fullSync = true
        )

        val config2 = config1.newConfig()

        Assert.assertEquals(testPath, config1.directory)
        Assert.assertTrue(config1.isFullSync)

        Assert.assertEquals(testPath, config2.directory)
        Assert.assertTrue(config2.isFullSync)
    }

    ///// ReplicatorConfiguration

    // Create config a config with no collection
    @Test
    fun testReplConfigNullCollections() {
        val target = testEndpoint
        val config = ReplicatorConfigurationFactory.newConfig(
            target = target,
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        Assert.assertEquals(target, config.target)
        Assert.assertEquals(ReplicatorType.PUSH, config.type)
        Assert.assertTrue(config.isContinuous)
        Assert.assertEquals(testAuthenticator, config.authenticator)
        Assert.assertEquals(testHeaders, config.headers)
        Assert.assertEquals(20, config.maxAttempts)
        Assert.assertEquals(100, config.heartbeat)
        Assert.assertEquals(false, config.isAutoPurgeEnabled)
    }

    // Create config with an empty collection
    @Test
    fun testReplConfigEmptyCollections() {
        val target = testEndpoint
        val config = ReplicatorConfigurationFactory.newConfig(
            collections = emptyMap(),
            target = target,
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        Assert.assertEquals(target, config.target)
        Assert.assertEquals(ReplicatorType.PUSH, config.type)
        Assert.assertTrue(config.isContinuous)
        Assert.assertEquals(testAuthenticator, config.authenticator)
        Assert.assertEquals(testHeaders, config.headers)
        Assert.assertEquals(20, config.maxAttempts)
        Assert.assertEquals(100, config.heartbeat)
        Assert.assertEquals(false, config.isAutoPurgeEnabled)
    }

    // Create config with explicitly configured default collection
    @Test
    fun testReplConfigCollectionsWithDefault() {
        val target = testEndpoint

        val collConfig1 = CollectionConfigurationFactory.newConfig(
            channels = testChannels,
            conflictResolver = testResolver,
            documentIDs = testDocIds,
            pushFilter = testPushFilter,
            pullFilter = testPullFilter
        )

        val collectionConfig = mapOf(setOf(testDatabase.defaultCollection) to collConfig1)
        val config = ReplicatorConfigurationFactory.newConfig(
            collections = collectionConfig,
            target = target,
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        Assert.assertEquals(collectionConfig.keys.first(), config.collections)
        Assert.assertEquals(target, config.target)
        Assert.assertEquals(ReplicatorType.PUSH, config.type)
        Assert.assertTrue(config.isContinuous)
        Assert.assertEquals(testAuthenticator, config.authenticator)
        Assert.assertEquals(testHeaders, config.headers)
        Assert.assertEquals(20, config.maxAttempts)
        Assert.assertEquals(100, config.heartbeat)
        Assert.assertEquals(false, config.isAutoPurgeEnabled)

        val collConfig2 = config.getCollectionConfiguration(testDatabase.defaultCollection)
        Assert.assertNotNull(collConfig2)
        Assert.assertNotSame(collConfig1, collConfig2)
        Assert.assertEquals(testChannels, collConfig2!!.channels)
        Assert.assertEquals(testResolver, collConfig2.conflictResolver)
        Assert.assertEquals(testDocIds, collConfig2.documentIDs)
        Assert.assertEquals(testPushFilter, collConfig2.pushFilter)
        Assert.assertEquals(testPullFilter, collConfig2.pullFilter)
    }

    // Create config with a configured non-default collection
    @Test
    fun testReplConfigCollectionsWithoutDefault() {
        val collConfig1 = CollectionConfigurationFactory.newConfig(
            channels = testChannels,
            conflictResolver = testResolver,
            documentIDs = testDocIds,
            pushFilter = testPushFilter,
            pullFilter = testPullFilter
        )
        val config = ReplicatorConfigurationFactory.newConfig(
            testEndpoint,
            mapOf(listOf(testCollection) to collConfig1),
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        Assert.assertEquals(testEndpoint, config.target)
        Assert.assertEquals(ReplicatorType.PUSH, config.type)
        Assert.assertTrue(config.isContinuous)
        Assert.assertEquals(testAuthenticator, config.authenticator)
        Assert.assertEquals(testHeaders, config.headers)
        Assert.assertEquals(20, config.maxAttempts)
        Assert.assertEquals(100, config.heartbeat)
        Assert.assertEquals(false, config.isAutoPurgeEnabled)

        val colls = config.collections
        Assert.assertNotNull(colls)
        Assert.assertEquals(1, colls.size)
        Assert.assertTrue(colls.contains(testCollection))

        val collConfig2 = config.getCollectionConfiguration(testCollection)

        Assert.assertNotNull(collConfig2)
        Assert.assertNotSame(collConfig1, collConfig2!!)
        Assert.assertEquals(testChannels, collConfig2.channels)
        Assert.assertEquals(testResolver, collConfig2.conflictResolver)
        Assert.assertEquals(testDocIds, collConfig2.documentIDs)
        Assert.assertEquals(testPushFilter, collConfig2.pushFilter)
        Assert.assertEquals(testPullFilter, collConfig2.pullFilter)
    }

    @Test
    fun testReplicatorConfigurationFactoryDataSources() {
        val config1 = ReplicatorConfigurationFactory.newConfig(testEndpoint)
        // config1 contains all default value

        Assert.assertEquals(testEndpoint, config1.target)
        Assert.assertEquals(ReplicatorType.PUSH_AND_PULL, config1.type)
        Assert.assertFalse(config1.isContinuous)
        Assert.assertNull(config1.authenticator)
        Assert.assertNull(config1.headers)
        Assert.assertEquals(Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT, config1.maxAttempts)
        Assert.assertEquals(Defaults.Replicator.HEARTBEAT, config1.heartbeat)
        Assert.assertTrue(config1.isAutoPurgeEnabled)

        val config2 = config1.newConfig(maxAttempts = 200)

        Assert.assertNotSame(config1, config2)
        Assert.assertEquals(Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT, config1.maxAttempts)
        Assert.assertEquals(200, config2.maxAttempts)
    }
}
