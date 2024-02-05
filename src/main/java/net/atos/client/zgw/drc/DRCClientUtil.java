package net.atos.client.zgw.drc;

import java.util.Base64;

import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObject;
import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObjectData;
import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObjectWithLockData;
import net.atos.client.zgw.drc.model.generated.Ondertekening;

public class DRCClientUtil {

    /**
     * Utility function to convert a byte array to a base64 string as
     * required by the ZGW DRC API.
     *
     * @param byteArray the byte array
     * @return the bas64 converted byte array as string
     */
    public static String convertByteArrayToBase64String(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    /**
     * Utility function to convert {@link EnkelvoudigInformatieObjectWithLockData.StatusEnum} to
     * {@link EnkelvoudigInformatieObject.StatusEnum}.
     * <br>
     * Eventhough they have the exact same values the OpenAPI Generator generates two
     * separate Java classes with two separate enums.
     */
    public static EnkelvoudigInformatieObject.StatusEnum convertToEnkelvoudigInformatieObjectStatusEnum(
            EnkelvoudigInformatieObjectWithLockData.StatusEnum statusEnum
    ) {
        return EnkelvoudigInformatieObject.StatusEnum.valueOf(statusEnum.name());
    }

    /**
     * Utility function to convert {@link EnkelvoudigInformatieObject.StatusEnum} to
     * {@link EnkelvoudigInformatieObjectData.StatusEnum}.
     * <br>
     * Eventhough they have the exact same values the OpenAPI Generator generates two
     * separate Java classes with two separate enums.
     */
    public static EnkelvoudigInformatieObjectData.StatusEnum convertToEnkelvoudigInformatieObjectDataStatusEnum(
            EnkelvoudigInformatieObject.StatusEnum statusEnum
    ) {
        return EnkelvoudigInformatieObjectData.StatusEnum.valueOf(statusEnum.name());
    }

    /**
     * Utility function to convert {@link EnkelvoudigInformatieObject.StatusEnum} to
     * {@link EnkelvoudigInformatieObjectData.StatusEnum}.
     * <br>
     * Eventhough they have the exact same values the OpenAPI Generator generates two
     * separate Java classes with two separate enums.
     */
    public static EnkelvoudigInformatieObjectWithLockData.StatusEnum convertToEnkelvoudigInformatieObjectWithLockDataStatusEnum(
            EnkelvoudigInformatieObject.StatusEnum statusEnum
    ) {
        return EnkelvoudigInformatieObjectWithLockData.StatusEnum.valueOf(statusEnum.name());
    }

    public static EnkelvoudigInformatieObjectWithLockData.VertrouwelijkheidaanduidingEnum
    convertToEnkelvoudigInformatieObjectWithLockDataVertrouwelijkheidaanduidingEnum(
            String vertrouwelijkheidaanduidingEnumString
    ) {
        return EnkelvoudigInformatieObjectWithLockData.VertrouwelijkheidaanduidingEnum.valueOf(vertrouwelijkheidaanduidingEnumString);
    }

    public static EnkelvoudigInformatieObject.VertrouwelijkheidaanduidingEnum convertToVertrouwelijkheidaanduidingEnum(
            String vertrouwelijkheidaanduidingEnumString
    ) {
        return EnkelvoudigInformatieObject.VertrouwelijkheidaanduidingEnum.valueOf(
                // we assume that the enum generated by the OpenAPI Generator is always
                // the exact uppercase value of the enum value in the OpenAPI specification
                vertrouwelijkheidaanduidingEnumString.toUpperCase()
        );
    }

    public static EnkelvoudigInformatieObjectData.VertrouwelijkheidaanduidingEnum convertToVertrouwelijkheidaanduidingEnumData(
            String vertrouwelijkheidaanduidingEnumString
    ) {
        return EnkelvoudigInformatieObjectData.VertrouwelijkheidaanduidingEnum.valueOf(
                // we assume that the enum generated by the OpenAPI Generator is always
                // the exact uppercase value of the enum value in the OpenAPI specification
                vertrouwelijkheidaanduidingEnumString.toUpperCase()
        );
    }

    public static boolean isOndertekend(EnkelvoudigInformatieObject enkelvoudigInformatieObject) {
        return enkelvoudigInformatieObject.getOndertekening() != null &&
                enkelvoudigInformatieObject.getOndertekening().getDatum() != null &&
                enkelvoudigInformatieObject.getOndertekening().getSoort() != null &&
                // this extra check is because the API can return an empty ondertekening soort
                // when no signature is present (even if this is not permitted according to the
                // original OpenAPI spec)
                !enkelvoudigInformatieObject.getOndertekening().getSoort().equals(
                        Ondertekening.SoortEnum.EMPTY
                );
    }
}
