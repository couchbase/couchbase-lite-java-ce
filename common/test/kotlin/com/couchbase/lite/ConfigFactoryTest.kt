//
// Copyright (c) 2020 Couchbase. All rights reserved.
// COUCHBASE CONFIDENTIAL - part of Couchbase Lite Enterprise Edition
//
package com.couchbase.lite

import com.couchbase.lite.internal.replicator.AbstractCBLWebSocket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun testDatabaseConfigurationFactory() {
        val config = DatabaseConfigurationFactory.newConfig(databasePath = testPath)
        assertEquals(testPath, config.directory)
    }

    @Test
    fun testFullTextIndexConfigurationFactoryCopyWithChanges() {
        val config1 = DatabaseConfigurationFactory.newConfig(databasePath = testPath)

        val config2 = config1.newConfig()

        assertEquals(testPath, config1.directory)

        assertEquals(testPath, config2.directory)
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

        assertEquals(target, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)
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

        assertEquals(target, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)
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

        assertEquals(collectionConfig.keys.first(), config.collections)
        assertEquals(target, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)

        val collConfig2 = config.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collConfig2)
        assertNotSame(collConfig1, collConfig2)
        assertEquals(testChannels, collConfig2!!.channels)
        assertEquals(testResolver, collConfig2.conflictResolver)
        assertEquals(testDocIds, collConfig2.documentIDs)
        assertEquals(testPushFilter, collConfig2.pushFilter)
        assertEquals(testPullFilter, collConfig2.pullFilter)
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

        assertEquals(testEndpoint, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)

        val colls = config.collections
        assertNotNull(colls)
        assertEquals(1, colls.size)
        assertTrue(colls.contains(testCollection))

        val collConfig2 = config.getCollectionConfiguration(testCollection)

        assertNotNull(collConfig2)
        assertNotSame(collConfig1, collConfig2!!)
        assertEquals(testChannels, collConfig2.channels)
        assertEquals(testResolver, collConfig2.conflictResolver)
        assertEquals(testDocIds, collConfig2.documentIDs)
        assertEquals(testPushFilter, collConfig2.pushFilter)
        assertEquals(testPullFilter, collConfig2.pullFilter)
    }

    @Test
    fun testReplicatorConfigurationFactoryDataSources() {
        val config1 = ReplicatorConfigurationFactory.newConfig(testEndpoint)
        // config1 contains all default value

        assertEquals(testEndpoint, config1.target)
        assertEquals(ReplicatorType.PUSH_AND_PULL, config1.type)
        assertFalse(config1.isContinuous)
        assertNull(config1.authenticator)
        assertNull(config1.headers)
        assertEquals(Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT, config1.maxAttempts)
        assertEquals(Defaults.Replicator.HEARTBEAT, config1.heartbeat)
        assertTrue(config1.isAutoPurgeEnabled)

        val config2 = config1.newConfig(maxAttempts = 200)

        assertNotSame(config1, config2)
        assertEquals(Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT, config1.maxAttempts)
        assertEquals(200, config2.maxAttempts)
    }
}
