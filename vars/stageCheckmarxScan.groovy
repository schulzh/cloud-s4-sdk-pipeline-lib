import com.sap.cloud.sdk.s4hana.pipeline.BuildToolEnvironment
import com.sap.cloud.sdk.s4hana.pipeline.QualityCheck
import com.sap.cloud.sdk.s4hana.pipeline.ReportAggregator
import com.sap.piper.ConfigurationLoader
import com.sap.piper.ConfigurationMerger

def call(Map parameters = [:]) {
    def stageName = 'checkmarxScan'
    def script = parameters.script
    def node = parameters.node
    runAsStage(stageName: stageName, script: script, node: node) {
        executeCheckmarxScan(script, stageName)
    }
}

private void executeCheckmarxScan( def script, String stageName) {

    final Map stageConfiguration = ConfigurationLoader.stageConfiguration(script, stageName)
    final Map stageDefaults = ConfigurationLoader.defaultStageConfiguration(script, stageName)

    Set stageConfigurationKeys = ['groupId',
                                  'vulnerabilityThresholdMedium',
                                  'checkMarxProjectName',
                                  'vulnerabilityThresholdLow',
                                  'filterPattern',
                                  'fullScansScheduled',
                                  'generatePdfReport',
                                  'incremental',
                                  'preset',
                                  'checkmarxCredentialsId',
                                  'checkmarxServerUrl']

    Map configuration = ConfigurationMerger.merge(stageConfiguration, stageConfigurationKeys, stageDefaults)

    // only applicable if customized config exists
    if (stageConfiguration) {
        configuration.script = script
        executeCheckmarxScan configuration
        ReportAggregator.instance.reportVulnerabilityScanExecution(QualityCheck.CheckmarxScan)
    }
}
