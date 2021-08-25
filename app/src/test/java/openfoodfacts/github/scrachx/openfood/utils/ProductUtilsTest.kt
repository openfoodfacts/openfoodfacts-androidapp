package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.Product
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as mockitoWhen

class ProductUtilsTest {
    @Test
    fun isPerServingInLiter() {
        val mockProd = mock(Product::class.java)

        mockitoWhen(mockProd.servingSize).thenReturn("3l")
        assertThat(mockProd.isPerServingInLiter()).isTrue()

        mockitoWhen(mockProd.servingSize).thenReturn("3oz")
        assertThat(mockProd.isPerServingInLiter()).isFalse()
    }

    @Test
    fun isBarcodeValid() {
        // Debug value
        assertThat(isBarcodeValid("1")).isTrue()

        // Incorrect values
        assertThat(isBarcodeValid("2")).isFalse()
        assertThat(isBarcodeValid("123456789")).isFalse()
        assertThat(isBarcodeValid("test")).isFalse()
        assertThat(isBarcodeValid("")).isFalse()
        assertThat(isBarcodeValid(null)).isFalse()

        // Correct values
        listOf(
            "0145342032309",
            "0175175578583",
            "0322155577405",
            "0358674785373",
            "0506054817125",
            "0561376737254",
            "0657942336843",
            "0750825365470",
            "0801804127975",
            "0825880142974",
            "0898939752400",
            "0961056892387",
            "1016113229206",
            "1074927495781",
            "1086496871279",
            "1091782955267",
            "1106147029320",
            "1150333987314",
            "1249666689399",
            "1275549839742",
            "1418210671474",
            "1805694243615",
            "1967796326341",
            "2221395906168",
            "2234808777491",
            "2411311486540",
            "2611991555660",
            "2756828784049",
            "2773961574146",
            "2917166527518",
            "2979061936277",
            "3003635315409",
            "3032399325867",
            "3103772604531",
            "3214009884104",
            "3242776624128",
            "3301011430341",
            "3548332192293",
            "3690327754723",
            "3709611127331",
            "3788579481488",
            "4065029673785",
            "4077959293308",
            "4312625539183",
            "4568377805230",
            "4777635292709",
            "4903228634469",
            "4943973222344",
            "4961649870675",
            "4967651071246",
            "5022746575784",
            "5282371311670",
            "5309593551216",
            "5313659462477",
            "5382196183237",
            "5594471216787",
            "5672573018802",
            "5764420563037",
            "5849597991446",
            "5922736502649",
            "5929241138424",
            "5978286037988",
            "6010692381398",
            "6373665996860",
            "6519293899412",
            "6569461437298",
            "6640751714159",
            "6665724382326",
            "6773707628740",
            "6804695088046",
            "6821454746627",
            "6862139506983",
            "7008864998207",
            "7121795039327",
            "7247006964018",
            "7370115312927",
            "7438839573120",
            "7442504334213",
            "7500430287062",
            "7881493080754",
            "8177704653695",
            "8204661397550",
            "8229180878090",
            "8303057031969",
            "8303447814455",
            "8379729621059",
            "8471062323680",
            "8515800131714",
            "8535251095243",
            "8706856976576",
            "8718646542731",
            "8954449076954",
            "8970370485641",
            "9359779239632",
            "9377823526866",
            "9389355637149",
            "9423513168776",
            "9510371184373",
            "9528722036004",
            "9634827116517"
        ).forEach { assertThat(isBarcodeValid(it)).isTrue() }
    }
}