import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification
import spock.lang.Ignore

public class SASTSpec extends JenkinsPipelineSpecification {

    def sast = null
    def script = '''/opt/CxConsolePlugin/runCxConsole.sh scan -v -ProjectName "test" \\
-CxServer "https://192.169.116.104" \\
-LocationType "folder" \\
-SASTHigh "5" \\
-SASTMedium "5" \\
-Preset "Java - High and Medium" \\
-ReportXML "cx_output.xml" \\
-LocationPath "~/workspace" \\
-TrustedCertificates \\
-Incremental \\
-CxUser "$CHECKMARX_UNAME" \\
-CxPassword "$CHECKMARX_PASS"'''

    static class DummyException extends RuntimeException {
        DummyException(String _message) { super( _message ) }
    }

    def setup() {
        sast = loadPipelineScriptForTest('checkmarx/steps/sast.groovy')
        sast.getBinding().setVariable("WORKSPACE", "~/workspace")
        sast.getBinding().setVariable("config", [ProjectName: "test",
                                                 CxCred: "TEST_USERNAME_PASSWORD",
                                                 Language: "Java"])
        explicitlyMockPipelineStep("inside_forge_pod")
        explicitlyMockPipelineStep('withChecks')
    }

    def "checkmarx script returns an error" () {
        when:
            sast()
        then:
            1 * getPipelineMock("usernamePassword.call")([credentialsId: "TEST_USERNAME_PASSWORD", usernameVariable: 'CHECKMARX_UNAME', passwordVariable: 'CHECKMARX_PASS'])
            1 * getPipelineMock("stage")("Static Analysis: Checkmarx", _)
            1 * getPipelineMock("sh")([script: script, returnStatus: true]) >> 3
            0 * getPipelineMock("sh")('cp /opt/CxConsolePlugin/Checkmarx/Reports/cx_output.xml ~/workspace')
    }

    def "checkmarx script archives report" () {
        when:
            sast()
        then:
            1 * getPipelineMock("usernamePassword.call")([credentialsId: "TEST_USERNAME_PASSWORD", usernameVariable: 'CHECKMARX_UNAME', passwordVariable: 'CHECKMARX_PASS'])
            1 * getPipelineMock("stage")("Static Analysis: Checkmarx", _)
            1 * getPipelineMock("sh")([script: script, returnStatus: true]) >> 0
            1 * getPipelineMock("sh")('cp /opt/CxConsolePlugin/Checkmarx/Reports/cx_output.xml ~/workspace')
    }

    @Ignore("FIXME: No error is reported at the moment")
    def "checkmarx fails when language is not supported" () {
        setup:
            sast.getBinding().setVariable("config", [ProjectName: "test",
                                                 CxCred: "TEST_USERNAME_PASSWORD",
                                                 Language: "ruby"])
        when:
            try {
                sast()
            } catch (DummyException e) {}
        then:
            1 * getPipelineMock("error")('Error: Unsupported language template - ruby') >> {throw new DummyException("Unsupport Language Tempalte")}
            0 * getPipelineMock("sh")([script: script, returnStatus: true])
    }
}
