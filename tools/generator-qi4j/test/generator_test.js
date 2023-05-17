/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

var path = require('path');
var helpers = require('yeoman-test');
var assert = require('yeoman-assert');
var shell = require('shelljs');

//See http://yeoman.io/authoring/testing.html
var restApiAppType = "Rest API";
var commandLineAppType = "Command Line";
var defaultAppType = restApiAppType;

var appTypes = [
    restApiAppType,
    commandLineAppType
];

var entityStores = [
    'BerkeleyDB',
    'Cassandra',
    'File',
    'DerbySQL',
    'Geode',
    'H2SQL',
    'Hazelcast',
    'JClouds',
    'Jdbm',
    'LevelDB',
    'MongoDB',
    'MySQL',
    'Preferences',
    'Redis',
    'Riak',
    'PostgreSQL',
    'SQLite',
    'Memory'   // Somehow the last EntityStore is used in subsequent test arrays. Pick the fastest.
];

var indexings = [
    'Rdf',
    'ElasticSearch',
    'Solr',
    'SQL'
];

var cachings = [
    'None',
    'Memcache',
    'EhCache'
];

var metricses = [
    'None',
    'Codahale'
];

var featuresset = [
    [],
    ['jmx'],
    ['mixin scripting'],
    ['jmx', 'mixin scripting'],
    ['security'],
    ['jmx', 'security'],
    ['mixin scripting', 'security'],
    ['jmx', 'mixin scripting', 'security'],
    ['envisage'],
    ['jmx', 'envisage'],
    ['mixin scripting', 'envisage'],
    ['jmx', 'mixin scripting', 'envisage'],
    ['security', 'envisage'],
    ['jmx', 'security', 'envisage'],
    ['mixin scripting', 'security', 'envisage'],
    ['jmx', 'mixin scripting', 'security', 'envisage']
];

// test with all defaults first.
test();

if (process.env.TEST_ALL === 'yes') {
    // All Tests !!!!
    appTypes.forEach(function (appType) {
        entityStores.forEach(function (entitystore) {
            indexings.forEach(function (indexing) {
                cachings.forEach(function (caching) {
                    metricses.forEach(function (metrics) {
                        featuresset.forEach(function (features) {
                            test(appType, entitystore, indexing, caching, metrics, features)
                        });
                    });
                });
            });
        });
    });
} else {
    // Subset
    appTypes.forEach(function (appType) {
        test(appType, "Memory", "Rdf", "Memcache", "Codahale", "[]");
    });

    entityStores.forEach(function (entityStore) {
        test(defaultAppType, entityStore, "Rdf", "Memcache", "Codahale", "[]");
    });

    indexings.forEach(function (indexing) {
        test(defaultAppType, "Memory", indexing, "Memcache", "Codahale", "[]");
    });

    cachings.forEach(function (caching) {
        test(defaultAppType, "Memory", "Rdf", caching, "Codahale", "[]");
    });

    metricses.forEach(function (metrics) {
        test(defaultAppType, "Memory", "Rdf", "Memcache", metrics, "[]");
    });

    featuresset.forEach(function (feature) {
        test(defaultAppType, "Memory", "Rdf", "Memcache", "Codahale", feature);
    });
}

function test(appType, entityStore, indexing, caching, metrics, features) {
    describe('qi4j-generator', function () {
        var testName = appType + ' with '
            + entityStore + ' EntityStore - '
            + indexing + ' Indexing - '
            + caching + ' Caching - '
            + metrics + ' Metrics';
        if (features && features.length > 0) {
            testName += ' - ' + features.toString().replace(new RegExp(',', 'g'), ' - ');
        }
        var testDirName = testName.replace(new RegExp(' - ', 'g'), '_').replace(new RegExp(' ', 'g'), '_');
        it(testName,
            function () {
                console.log("\n\nTest: " + testName);
                this.timeout(60000);
                return helpers.run(path.join(__dirname, '../app'))
                    .inDir(path.join(__dirname, '../build/npm-test/' + testDirName))
                    .withPrompts({
                        name: 'TestProject',
                        packageName: 'org.qi4j.generator.test',
                        applicationtype: appType,
                        entitystore: entityStore,
                        indexing: indexing,
                        caching: caching,
                        metrics: metrics,
                        features: features
                    })
                    .then(buildAndVerify);
            });
    });
}

function buildAndVerify(dir) {
    assert.file(['gradlew', 'settings.gradle', 'build.gradle']);
    assert(shell.exec(path.join(dir, 'gradlew') + ' check --init-script ../../stagedMavenRepoInitScript.gradle').code == 0);
}
