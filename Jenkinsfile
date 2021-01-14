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

        stage('Build_Prod'){
            when{
                  environment name: '_deployEnv' ,value: 'prod'
            }
            steps{
                 //验证目录是否存在，不存在则创建
                 sh "sshpass -p ${_deployMap['_targetPwd']} ssh -p ${_deployMap['_targetPort']} \"[ -d ${_buildPath}/${env.JOB_NAME}/ ] && `echo '1 < 2'` || mkdir -p ${_buildPath}/${env.JOB_NAME}/\""
                 //远程传输jar包和启动脚本
                 sh "sshpass -p ${_deployMap['_targetPwd']} scp -P ${_deployMap['_targetPort']} ${env.WORKSPACE}/target/${env.JOB_NAME}.jar ${_buildPath}/install.sh ${_deployMap['_targetUser']}@${_deployMap['_targetIP']}:${_buildPath}/${env.JOB_NAME}/"
                 script{
                         stage('Remote Prod'){   //远程执行脚本文件
                             def remote= [:]
                             remote.name="root"
                             remote.host="${_deployMap['_targetIP']}"
                             remote.user="${_deployMap['_targetUser']}"
                             remote.password="${_deployMap['_targetPwd']}"
                             remote.port=Integer.parseInt("${_deployMap['_targetPort']}")
                             remote.allowAnyHosts= true
                             //修改启动脚本模板内容
                             sshCommand remote: remote, command: "cd ${_buildPath}/${env.JOB_NAME}/ && sed -i \"s|service_name_tmp|${env.JOB_NAME}|g\" install.sh"
                             sshCommand remote: remote, command: "cd ${_buildPath}/${env.JOB_NAME}/ && sed -i \"s|service_path_tmp|${_buildPath}|g\" install.sh"
                             sshCommand remote: remote, command: "cd ${_buildPath}/${env.JOB_NAME}/ && sed -i \"s|java_home_tmp|`echo $JAVA_HOME`|g\" install.sh && chmod a+x install.sh"
                             sshCommand remote: remote, command: "nohup ${_buildPath}/${env.JOB_NAME}/install.sh > ${_buildPath}/${env.JOB_NAME}/install.log 2>&1 &"
                         }
                  }
            }
        }
    }
}