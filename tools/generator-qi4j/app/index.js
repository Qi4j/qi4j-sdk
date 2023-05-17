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
 *
 *
 */

var generators = require('yeoman-generator');
var fs = require('fs');

var qi4jVersion = require(__dirname + '/../package.json').qi4j_version;

var qi4j = {};

module.exports = generators.Base.extend(
    {
        constructor: function () {
            console.log("WARNING!!!  This is BETA quality and likely to change drastically over time. "); // + JSON.stringify(arguments));
            generators.Base.apply(this, arguments);

            this.option('import-model', {
                name: "import",
                desc: 'Reads a model file and creates the domain model for it.',
                type: String,
                default: "./model.json",
                hide: false
            });

            this.option('export-model', {
                name: "export",
                desc: 'Writes the model of the application into a json file.',
                type: String,
                default: "exported-model",
                hide: false
            });

            this.option('noPrompt', {
                name: "noPrompt",
                desc: 'If specified, the interactive prompts will be disabled.',
                type: Boolean,
                default: false,
                hide: false
            });

            if (this.options.import) {
                qi4j = importModel(this.options.import);
                qi4j.name = qi4j.name ? qi4j.name : firstUpper(this.appname);
                qi4j.packageName = qi4j.packageName ? qi4j.packageName : ("com.acme." + this.appname);
                qi4j.applicationtype = "Rest API";
                qi4j.features = qi4j.features ? qi4j.features : [];
                qi4j.modules = qi4j.modules ? qi4j.modules : {};
                qi4j.indexing = qi4j.indexing ? qi4j.indexing : null;
                qi4j.entitystore = qi4j.entitystore ? qi4j.entitystore : null;
                qi4j.caching = qi4j.caching ? qi4j.caching : null;
                qi4j.dbpool = qi4j.dbpool === undefined ? "DBCP" : answers.dbpool;
            }
        },

        prompting: function () {
            if (this.options.noPrompt) {
                return this.prompt([]);
            }
            else {
                return this.prompt(
                    [
                        {
                            type: 'input',
                            name: 'name',
                            message: 'Your project name',
                            default: qi4j.name ? qi4j.name : firstUpper(this.appname)
                        },
                        {
                            type: 'input',
                            name: 'packageName',
                            message: 'Java package name',
                            default: qi4j.packageName ? qi4j.packageName : "com.acme"
                        },
                        {
                            type: 'list',
                            name: 'applicationtype',
                            choices: [
                                'Command Line',
                                // 'Web Application',
                                'Rest API'
                            ],
                            message: 'what type of application do you want to create?',
                            default: qi4j.applicationtype ? qi4j.applicationtype : "Rest API"
                        },
                        {
                            type: 'list',
                            name: 'entitystore',
                            choices: [
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
                                'Memory',
                                'MongoDB',
                                'MySQL',
                                'Preferences',
                                'Redis',
                                'Riak',
                                'PostgreSQL',
                                'SQLite'
                            ],
                            message: 'Which entity store do you want to use?',
                            default: qi4j.entitystore ? qi4j.entitystore : "Memory"
                        },
                        {
                            type: 'list',
                            name: 'dbpool',
                            choices: [
                                'BoneCP',
                                'DBCP'
                            ],
                            message: 'Which connection pool do you want to use?',
                            default: qi4j.dbpool ? qi4j.dbpool : "DBCP",
                            when: function (answers) {
                                return answers.entitystore.indexOf('SQL') > -1;
                            }
                        },
                        {
                            type: 'list',
                            name: 'indexing',
                            choices: [
                                'Rdf',
                                'ElasticSearch',
                                'Solr',
                                'SQL'
                            ],
                            message: 'Which indexing system do you want to use?',
                            default: qi4j.indexing ? qi4j.indexing : "Rdf"
                        },
                        {
                            type: 'list',
                            name: 'caching',
                            choices: [
                                'None',
                                'Memcache',
                                'EhCache'
                            ],
                            message: 'Which caching system do you want to use?',
                            default: qi4j.caching ? qi4j.caching : "None"
                        },
                        {
                            type: 'list',
                            name: 'metrics',
                            choices: [
                                'None',
                                'Codahale'
                            ],
                            message: 'Which metrics capturing system do you want to use?',
                            default: qi4j.metrics ? qi4j.metrics : "None"
                        },
                        {
                            type: 'checkbox',
                            name: 'features',
                            choices: [
                                // 'alarms'
                                // 'circuit breakers'
                                'envisage',
                                // 'file transactions'
                                // 'logging'
                                'jmx',
                                // 'spring integration'
                                // 'scheduling'
                                'mixin scripting',
                                'security'
                                // ,'version migration'
                            ],
                            message: 'Other features?',
                            default: qi4j.features ? qi4j.features : []
                        }
                    ]
                ).then(function (answers) {
                        this.log('app name', answers.name);
                        this.log('Entity Stores:', answers.entitystore);
                        this.log('Indexing:', answers.indexing);
                        this.log('Caching:', answers.caching);
                        this.log('Metrics:', answers.metrics);
                        this.log('Features:', answers.features);
                        qi4j.name = answers.name;
                        qi4j.packageName = answers.packageName;
                        qi4j.applicationtype = answers.applicationtype;
                        qi4j.features = answers.features;
                        qi4j.indexing = answers.indexing;
                        qi4j.entitystore = answers.entitystore;
                        qi4j.dbpool = answers.dbpool === undefined ? "DBCP" : answers.dbpool;
                        qi4j.metrics = answers.metrics;
                        qi4j.caching = answers.caching;
                    }.bind(this)
                );
            }
        },

        writing: function () {
            try {
                qi4j.version = qi4jVersion;
                qi4j.entitystoremodule = qi4j.entitystore.toLowerCase();
                if (qi4j.entitystore === "DerbySQL") {
                    qi4j.entitystoremodule = "sql";
                }
                if (qi4j.entitystore === "H2SQL") {
                    qi4j.entitystoremodule = "sql";
                }
                if (qi4j.entitystore === "MySQL") {
                    qi4j.entitystoremodule = "sql";
                }
                if (qi4j.entitystore === "PostgreSQL") {
                    qi4j.entitystoremodule = "sql";
                }
                if (qi4j.entitystore === "SQLite") {
                    qi4j.entitystoremodule = "sql";
                }
                assignFunctions(qi4j);
                qi4j.javaPackageDir = qi4j.packageName.replace(/[.]/g, '/');
                qi4j.ctx = this;
                var app = require(__dirname + '/templates/' + qi4j.applicationtype.replace(/ /g, '') + 'Application/app.js');
                app.write(qi4j);
                var buildToolChain = require(__dirname + '/templates/buildtool/build.js');
                buildToolChain.write(qi4j);
                if (this.options.export) {
                    exportModel(this.options.export);
                }
            } catch (exception) {
                console.log(exception);
                throw exception;
            }
        }
    }
);

function hasFeature(feature) {
    return qi4j.features.indexOf(feature) >= 0;
}

function firstUpper(text) {
    return text.charAt(0).toUpperCase() + text.substring(1);
}

function importModel(filename) {
    if (typeof filename !== 'string') {
        filename = "./model.json";
    }
    return JSON.parse(fs.readFileSync(filename, 'utf8'));
}

function exportModel(filename) {
    if (typeof filename !== 'string') {
        filename = "exported-model.json";
    }
    delete qi4j.current;
    return fs.writeFileSync(filename, JSON.stringify(qi4j, null, 4) + "\n", 'utf8');
}

function assignFunctions(qi4j) {

    qi4j.hasFeature = function (feature) {
        return qi4j.features.indexOf(feature) >= 0;
    };

    qi4j.copyToConfig = function (ctx, from, toName) {
        qi4j.copyTemplate(ctx,
            from,
            'app/src/dist/config/development/' + toName);
        qi4j.copyTemplate(ctx,
            from,
            'app/src/dist/config/qa/' + toName);
        qi4j.copyTemplate(ctx,
            from,
            'app/src/dist/config/staging/' + toName);
        qi4j.copyTemplate(ctx,
            from,
            'app/src/dist/config/production/' + toName);
        qi4j.copyTemplate(ctx,
            from,
            'app/src/test/resources/' + toName);
    };

    qi4j.copyTemplate = function (ctx, from, to) {
        try {

            ctx.fs.copyTpl(
                ctx.templatePath(from),
                ctx.destinationPath(to),
                {
                    hasFeature: hasFeature,
                    firstUpper: firstUpper,
                    qi4j: qi4j
                }
            );
        } catch (exception) {
            console.log("Unable to copy template: " + from + "\n", exception);
        }
    };

    qi4j.copyBinary = function (ctx, from, to) {

        try {
            ctx.fs.copy(
                ctx.templatePath(from),
                ctx.destinationPath(to));
        } catch (exception) {
            console.log("Unable to copy binary: " + from + " to " + to + "\n", exception);
        }
    };

    qi4j.copyQi4jBootstrap = function (ctx, layer, moduleName, condition) {
        if (condition) {
            copyTemplate(ctx,
                moduleName + '/bootstrap.tmpl',
                'bootstrap/src/main/java/' + qi4j.javaPackageDir + '/bootstrap/' + layer + '/' + moduleName + '.java');
        }
    };

    qi4j.copyEntityStore = function (ctx, entityStoreName) {
        copyTemplate(ctx,
            'StorageModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + qi4j.javaPackageDir + '/bootstrap/infrastructure/' + entityStoreName + 'StorageModule.java');
    };

    qi4j.copyModules = function (dirname) {
        fs.readdir(dirname, function (err, files) {
            if (files !== undefined) {
                files.forEach(function (directory) {
                    if (directory.endsWith("Module")) {
                        var module = require(dirname + "/" + directory + '/module.js');
                        module.write(qi4j);
                    }
                });
            }
        });
    };
    qi4j.firstUpper = function (text) {
        return text.charAt(0).toUpperCase() + text.substring(1);
    };
    qi4j.typeNameOnly = function (text) {
        var lastPos = text.lastIndexOf(".");
        if (lastPos < 0) {
            return text;
        }
        return text.substring(lastPos + 1);
    };

    qi4j.configurationClassName = function (clazzName) {
        if (clazzName.endsWith("Service")) {
            clazzName = clazzName.substring(0, clazzName.length - 7);
        }
        return clazzName + "Configuration";
    };

    qi4j.prepareClazz = function (current) {
        var state = [];
        var imported = {};
        var props = current.clazz.properties;
        var idx;
        var assoc;
        if (props) {
            imported["org.qi4j.api.property.Property"] = true;
            for (idx in props) {
                if (props.hasOwnProperty(idx)) {
                    var prop = props[idx];
                    state.push('Property' + '<' + qi4j.typeNameOnly(prop.type) + "> " + prop.name + "();");
                    imported[prop.type] = true;
                }
            }
        } else {
            imported["org.qi4j.api.property.Property"] = true;
            state.push('Property<String> name();    // TODO: remove sample property')
        }
        var assocs = current.clazz.associations;
        if (assocs) {
            imported["org.qi4j.api.association.Association"] = true;
            for (idx in assocs) {
                if (assocs.hasOwnProperty(idx)) {
                    assoc = assocs[idx];
                    state.push("Association" + '<' + qi4j.typeNameOnly(assoc.type) + "> " + assoc.name + "();");
                    imported[assoc.type] = true;
                }
            }
        }
        assocs = current.clazz.manyassociations;
        if (assocs) {
            imported["org.qi4j.api.association.ManyAssociation"] = true;
            for (idx in assocs) {
                if (assocs.hasOwnProperty(idx)) {
                    assoc = assocs[idx];
                    state.push("ManyAssociation<" + qi4j.typeNameOnly(assoc.type) + "> " + assoc.name + "();");
                    imported[assoc.type] = true;
                }
            }
        }
        assocs = current.clazz.namedassociations;
        if (assocs) {
            imported["org.qi4j.api.association.NamedAssociation"] = true;
            for (idx in assocs) {
                if (assocs.hasOwnProperty(idx)) {
                    assoc = assocs[idx];
                    state.push("NamedAssociation<" + qi4j.typeNameOnly(assoc.type) + "> " + assoc.name + "();");
                    imported[assoc.type] = true;
                }
            }
        }
        current.state = state;
        current.imported = imported;
    };

    qi4j.prepareConfigClazz = function (currentModule, composite) {
        var state = [];
        var propertyFile = [];
        var imported = {};
        var props = composite.configuration;
        if (props) {
            imported["org.qi4j.api.property.Property"] = true;
            for (var idx in props) {
                if (props.hasOwnProperty(idx)) {
                    var prop = props[idx];
                    imported[prop.type] = true;
                    var propertyDefault;
                    if (prop.default !== undefined) {
                        propertyDefault = prop.default;
                    } else {
                        if (prop.type === "java.lang.String") {
                            propertyDefault = '';
                        }
                        else if (prop.type === "java.lang.Boolean") {
                            propertyDefault = 'false';
                        }
                        else if (prop.type === "java.lang.Long") {
                            propertyDefault = '0';
                        }
                        else if (prop.type === "java.lang.Integer") {
                            propertyDefault = '0';
                        }
                        else if (prop.type === "java.lang.Double") {
                            propertyDefault = '0.0';
                        }
                        else if (prop.type === "java.lang.Float") {
                            propertyDefault = '0.0';
                        }
                        else {
                            propertyDefault = '\n    # TODO: complex configuration type. ';
                        }
                    }
                    state.push("/**");
                    for (var idxDesc in prop.description) {
                        if (prop.description.hasOwnProperty(idxDesc)) {
                            var desc = prop.description[idxDesc];
                            propertyFile.push("# " + desc);
                            state.push(" * " + desc)
                        }
                    }
                    state.push(" */");
                    propertyFile.push(prop.name + "=" + propertyDefault + "\n");
                    state.push('Property' + '<' + qi4j.typeNameOnly(prop.type) + "> " + prop.name + "();\n");
                }
            }
        } else {
            imported["org.qi4j.api.property.Property"] = true;
            state.push('/** TODO: remove sample property');
            state.push(' */');
            state.push('Property<String> name();');
            propertyFile.push("# This is just the sample configuration value. ");
            propertyFile.push("# TODO: Remove this config value ");
            propertyFile.push('name=sample config value');
        }
        currentModule.state = state;
        currentModule.propertyLines = propertyFile;
        currentModule.imported = imported;
    };
}
