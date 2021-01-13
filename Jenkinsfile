pipeline{
    agent any
    parameters{
        choice(name: "_BUILD_ENV",choices: ['dev','prod','test'],description: "选择构建环境")
    }
    tools{
        maven 'maven'
        jdk 'java'
    }
    environment{
        _buildPath = "/server/service"
        _deployEnv = "${_BUILD_ENV}"
        _devHost = "192.10.169.201"
        _testHost = "192.10.169.212"
        _devCredentialsId = "201"
        _testCredentialsId = "212"
        _prodCredentialsId = ''
        _deployMap = ''
    }
    stages{
        stage('Build'){
            steps{
                sh "mvn clean package -D maven.test.skip=true -P${_BUILD_ENV}"
            }
        }

        stage('Deploy_Dev'){
            when{
                environment name: '_deployEnv' ,value: 'dev'
            }

            steps{
                sh "sh ${_buildPath}/${env.JOB_NAME}/install.sh"

            }
        }

        stage('Deploy_Test'){
            when{
                environment name: '_deployEnv' ,value: 'test'
            }
            environment{
                BITBUCKET_CREDS = credentials("${_testCredsId}")
            }
            steps{
                sh "sshpass -p ${BITBUCKET_CREDS_PSW} scp ${env.WORKSPACE}/target/${env.JOB_NAME}.jar ${BITBUCKET_CREDS_USR}@${_testHost}:${_buildPath}/${env.JOB_NAME}"
                script{
                    stage('Remote Test'){   //远程执行脚本文件
                        def remote= [:]
                        remote.name="root"
                        remote.host="${_testHost}"
                        remote.user="${BITBUCKET_CREDS_USR}"
                        remote.password="${BITBUCKET_CREDS_PSW}"
                        remote.allowAnyHosts= true
                        writeFile file:'install.sh', text: "nohup ${_buildPath}/${env.JOB_NAME}/install.sh > ${_buildPath}/${env.JOB_NAME}/install.log 2>&1 &"
                        sshScript remote: remote,script: "install.sh"
                    }
                }
            }
        }

        stage('Deploy_Prod'){
            when{
                environment name: '_deployEnv' ,value: 'prod'
            }
            steps{
                script{
                    _deployMap = input (
                        message: '发布新环境的配置',
                        ok: '确定',
                        parameters: [
                            string(defaultValue: '',description: '目标服务器ip',name: '_targetIP'),
                            string(defaultValue: '22',description: '目标服务器端口',name: '_targetPort'),
                            string(defaultValue: 'root',description: '目标服务器用户名',name: '_targetUser'),
                            password(defaultValue: 'SECRET',description: '目标服务器密码',name: '_targetPwd'),
                        ]
                    )
                }
            }
        }

        stage('SCP'){
            sh "sshpass -p ${_deployMap['_targetPort']} scp ${env.WORKSPACE}/target/${env.JOB_NAME}.jar ${_deployMap['_targetUser']}@${_deployMap['_targetIP']}:${_buildPath}/${env.JOB_NAME}"
        }
    }
}