import com.sap.cloud.sdk.s4hana.pipeline.BuildToolEnvironment
import com.sap.cloud.sdk.s4hana.pipeline.ReportAggregator
import com.sap.piper.ConfigurationLoader

def call(Map parameters = [:]) {
    def stageName = 's4SdkQualityChecks'
    def script = parameters.script
    runAsStage(stageName: stageName, script: script) {
        Map stageConfiguration = ConfigurationLoader.stageConfiguration(script, stageName)

        runOverModules(script: script, moduleType: "java") { String basePath ->
            executeQualityChecks(script, basePath, stageConfiguration)
        }
    }
}

private void executeQualityChecks(def script, String basePath, Map configuration) {

    if (BuildToolEnvironment.instance.isMaven() || BuildToolEnvironment.instance.isMta()) {
        checkDeploymentDescriptors script: script

        checkDependencies script: script, basePath: basePath

        aggregateListenerLogs()

        checkResilience()
        ReportAggregator.instance.reportResilienceCheck()

        checkServices script: script, nonErpDestinations: configuration.nonErpDestinations, customODataServices: configuration.customODataServices
        ReportAggregator.instance.reportServicesCheck(configuration.nonErpDestinations, configuration.customODataServices)
    }

    checkCodeCoverage(
        script: script,
        jacocoExcludes: configuration.jacocoExcludes,
        codeCoverageFrontend: configuration.codeCoverageFrontend,
        basePath: basePath
    )
}
