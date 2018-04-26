import com.sap.cloud.sdk.s4hana.pipeline.DownloadCacheUtils

def call(Map parameters = [:]) {
    handleStepErrors(stepName: 'executeWhitesourceScanNpm', stepParameters: parameters) {
        final script = parameters.script

        try {
            executeNpm(script: script, dockerOptions: DownloadCacheUtils.downloadCacheNetworkParam()) {
                sh """
                npm install whitesource
                alias whitesource='node node_modules/whitesource/bin/whitesource.js'
                whitesource run
                """.trim()
            }
        } finally {
            archiveArtifacts artifacts: 'ws-l*', allowEmptyArchive: true
        }
    }
}
