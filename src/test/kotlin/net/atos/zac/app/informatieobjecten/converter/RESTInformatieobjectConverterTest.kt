package net.atos.zac.app.informatieobjecten.converter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.date.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import jakarta.enterprise.inject.Instance
import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObjectData
import net.atos.client.zgw.ztc.ZTCClientService
import net.atos.client.zgw.ztc.model.createInformatieObjectType
import net.atos.zac.app.informatieobjecten.model.createRESTEnkelvoudigInformatieobject
import net.atos.zac.app.informatieobjecten.model.createRESTFileUpload
import net.atos.zac.app.taken.model.createRESTTaakDocumentData
import net.atos.zac.authentication.LoggedInUser
import net.atos.zac.authentication.createLoggedInUser
import java.time.LocalDate
import java.util.*

class RESTInformatieobjectConverterTest : BehaviorSpec() {
    private val loggedInUserInstance = mockk<Instance<LoggedInUser>>()
    private val ztcClientService = mockk<ZTCClientService>()
    private val loggedInUser = createLoggedInUser()

    // We have to use @InjectMockKs since the class under test uses field injection instead of constructor injection.
    // This is because WildFly does not support constructor injection for JAX-RS REST services completely.
    @InjectMockKs
    lateinit var restInformatieobjectConverter: RESTInformatieobjectConverter

    override suspend fun beforeContainer(testCase: TestCase) {
        super.beforeContainer(testCase)

        // Only run before Given
        if (testCase.parent != null) return

        MockKAnnotations.init(this)
        clearAllMocks()

        every { loggedInUserInstance.get() } returns loggedInUser
    }

    init {
        Given("REST taak document data and REST file upload are provided") {
            val restTaakDocumentData = createRESTTaakDocumentData()
            val restFileUpload = createRESTFileUpload()
            val providedInformatieObjectType = createInformatieObjectType()

            every {
                ztcClientService.readInformatieobjecttype(restTaakDocumentData.documentType.uuid)
            } returns providedInformatieObjectType

            When("convert is invoked") {
                val enkelvoudigInformatieObjectData = restInformatieobjectConverter.convert(
                    restTaakDocumentData,
                    restFileUpload
                )
                Then("the provided data is converted correctly") {
                    with(enkelvoudigInformatieObjectData) {
                        // currently hardcoded
                        bronorganisatie shouldBe "123443210"
                        creatiedatum shouldHaveSameDayAs LocalDate.now()
                        titel shouldBe restTaakDocumentData.documentTitel
                        auteur shouldBe loggedInUser.fullName
                        // currently hardcoded
                        taal shouldBe "dut"
                        informatieobjecttype shouldBe providedInformatieObjectType.url
                        inhoud shouldBe Base64.getEncoder().encodeToString(restFileUpload.file)
                        formaat shouldBe restFileUpload.type
                        bestandsnaam shouldBe restFileUpload.filename
                        // status should always be DEFINITIEF
                        status shouldBe EnkelvoudigInformatieObjectData.StatusEnum.DEFINITIEF
                        vertrouwelijkheidaanduiding shouldBe EnkelvoudigInformatieObjectData.VertrouwelijkheidaanduidingEnum.valueOf(
                            restTaakDocumentData.documentType.vertrouwelijkheidaanduiding
                        )
                    }
                }
            }
        }
        Given("REST enkelvoudig informatie object data and REST file upload are provided") {
            val restEnkelvoudigInformatieobject = createRESTEnkelvoudigInformatieobject()
            val restFileUpload = createRESTFileUpload()
            val providedInformatieObjectType = createInformatieObjectType()

            every {
                ztcClientService.readInformatieobjecttype(restEnkelvoudigInformatieobject.informatieobjectTypeUUID)
            } returns providedInformatieObjectType

            When("convert taak object is invoked") {
                val enkelvoudigInformatieObjectData = restInformatieobjectConverter.convertTaakObject(
                    restEnkelvoudigInformatieobject,
                    restFileUpload
                )
                Then("the provided data is converted correctly") {
                    with(enkelvoudigInformatieObjectData) {
                        // currently hardcoded
                        bronorganisatie shouldBe "123443210"
                        creatiedatum shouldHaveSameDayAs LocalDate.now()
                        titel shouldBe restEnkelvoudigInformatieobject.titel
                        auteur shouldBe loggedInUser.fullName
                        // currently hardcoded
                        taal shouldBe "dut"
                        informatieobjecttype shouldBe providedInformatieObjectType.url
                        inhoud shouldBe Base64.getEncoder().encodeToString(restFileUpload.file)
                        formaat shouldBe restFileUpload.type
                        bestandsnaam shouldBe restFileUpload.filename
                        // status should always be DEFINITIEF
                        status shouldBe EnkelvoudigInformatieObjectData.StatusEnum.DEFINITIEF
                        // vertrouwelijkheidaanduiding should always be OPENBAAR
                        vertrouwelijkheidaanduiding shouldBe EnkelvoudigInformatieObjectData.VertrouwelijkheidaanduidingEnum.OPENBAAR
                    }
                }
            }
        }
        Given("REST enkelvoudig informatie object data and REST file upload are provided") {
            // when converting a zaak more fields in the RESTEnkelvoudigInformatieobject are used in the
            // conversion compared to when converting a taak
            val restEnkelvoudigInformatieobject = createRESTEnkelvoudigInformatieobject(
                vertrouwelijkheidaanduiding = "vertrouwelijk",
                creatieDatum = LocalDate.now(),
                auteur = "dummyAuteur",
                taal = "dummyTaal",
                bestandsNaam = "dummyBestandsNaam"
            )
            val restFileUpload = createRESTFileUpload()
            val providedInformatieObjectType = createInformatieObjectType()

            every {
                ztcClientService.readInformatieobjecttype(restEnkelvoudigInformatieobject.informatieobjectTypeUUID)
            } returns providedInformatieObjectType

            When("convert zaak object is invoked") {
                val enkelvoudigInformatieObjectData = restInformatieobjectConverter.convertZaakObject(
                    restEnkelvoudigInformatieobject,
                    restFileUpload
                )
                Then("the provided data is converted correctly") {
                    with(enkelvoudigInformatieObjectData) {
                        // currently hardcoded
                        bronorganisatie shouldBe "123443210"
                        creatiedatum shouldHaveSameDayAs LocalDate.now()
                        titel shouldBe restEnkelvoudigInformatieobject.titel
                        auteur shouldBe restEnkelvoudigInformatieobject.auteur
                        taal shouldBe restEnkelvoudigInformatieobject.taal
                        informatieobjecttype shouldBe providedInformatieObjectType.url
                        inhoud shouldBe Base64.getEncoder().encodeToString(restFileUpload.file)
                        formaat shouldBe restFileUpload.type
                        bestandsnaam shouldBe restEnkelvoudigInformatieobject.bestandsnaam
                        status.value() shouldBe restEnkelvoudigInformatieobject.status.value()
                        vertrouwelijkheidaanduiding.value() shouldBe restEnkelvoudigInformatieobject.vertrouwelijkheidaanduiding
                    }
                }
            }
        }
    }
}
