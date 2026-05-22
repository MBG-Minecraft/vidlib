package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubPaths;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.neoforged.fml.loading.FMLLoader;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public interface HubCountries {
	Lazy<Path> LOCAL_PATH = HubPaths.DATA_DIRECTORY.<Path>map(path -> path.resolve("countries.json"));

	Lazy<CountryList> REMOTE = Lazy.of(() -> {
		try {
			var request = HubAPI.HTTP_CLIENT.send(HubAPI.apiCountries(), HttpResponse.BodyHandlers.ofInputStream());
			var checksum = request.headers().firstValue("X-Checksum").orElse("");

			if (!checksum.isEmpty()) {
				try (var in = request.body()) {
					var json = JsonUtils.read(in);
					var countryList = CountryList.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

					if (countryList != CountryList.EMPTY) {
						var path = CommonPaths.mkdirs(LOCAL_PATH.get());
						JsonUtils.write(path, CountryList.CODEC.encodeStart(JsonOps.INSTANCE, countryList).getOrThrow(), false);
					}

					return countryList;
				}
			}
		} catch (Exception ignored) {
		}

		return CountryList.EMPTY;
	});

	Lazy<CountryList> LOADED = Lazy.of(() -> {
		var path = LOCAL_PATH.get();
		var countryList = CountryList.EMPTY;

		if (Files.exists(path)) {
			try {
				var json = JsonUtils.read(path);
				countryList = CountryList.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			} catch (Exception ignored) {
			}
		}

		if (countryList == CountryList.EMPTY) {
			countryList = REMOTE.get();

			if (!FMLLoader.isProduction()) {
				VidLib.LOGGER.info("Fetched country list " + countryList.checksum() + ":");

				for (var country : countryList.byCode().values()) {
					VidLib.LOGGER.info("Supplier<Country> %s = add(\"%s\");".formatted(country.cca2(), country.code()));
				}
			}
		}

		return countryList;
	});

	Map<String, Lazy<HubCountry>> CACHE = new Object2ObjectOpenHashMap<>();

	static Supplier<HubCountry> get(String code) {
		return CACHE.computeIfAbsent(code, c -> LOADED.<HubCountry>map(map -> map.byCode().get(code)));
	}

	Supplier<HubCountry> AF = get("af");
	Supplier<HubCountry> AL = get("al");
	Supplier<HubCountry> DZ = get("dz");
	Supplier<HubCountry> AS = get("as");
	Supplier<HubCountry> AD = get("ad");
	Supplier<HubCountry> AO = get("ao");
	Supplier<HubCountry> AI = get("ai");
	Supplier<HubCountry> AQ = get("aq");
	Supplier<HubCountry> AG = get("ag");
	Supplier<HubCountry> AR = get("ar");
	Supplier<HubCountry> AM = get("am");
	Supplier<HubCountry> AW = get("aw");
	Supplier<HubCountry> AU = get("au");
	Supplier<HubCountry> AT = get("at");
	Supplier<HubCountry> AZ = get("az");
	Supplier<HubCountry> BS = get("bs");
	Supplier<HubCountry> BH = get("bh");
	Supplier<HubCountry> BD = get("bd");
	Supplier<HubCountry> BB = get("bb");
	Supplier<HubCountry> BY = get("by");
	Supplier<HubCountry> BE = get("be");
	Supplier<HubCountry> BZ = get("bz");
	Supplier<HubCountry> BJ = get("bj");
	Supplier<HubCountry> BM = get("bm");
	Supplier<HubCountry> BT = get("bt");
	Supplier<HubCountry> BO = get("bo");
	Supplier<HubCountry> BA = get("ba");
	Supplier<HubCountry> BW = get("bw");
	Supplier<HubCountry> BV = get("bv");
	Supplier<HubCountry> BR = get("br");
	Supplier<HubCountry> IO = get("io");
	Supplier<HubCountry> VG = get("vg");
	Supplier<HubCountry> BN = get("bn");
	Supplier<HubCountry> BG = get("bg");
	Supplier<HubCountry> BF = get("bf");
	Supplier<HubCountry> BI = get("bi");
	Supplier<HubCountry> KH = get("kh");
	Supplier<HubCountry> CM = get("cm");
	Supplier<HubCountry> CA = get("ca");
	Supplier<HubCountry> CV = get("cv");
	Supplier<HubCountry> BQ = get("bq");
	Supplier<HubCountry> KY = get("ky");
	Supplier<HubCountry> CF = get("cf");
	Supplier<HubCountry> TD = get("td");
	Supplier<HubCountry> CL = get("cl");
	Supplier<HubCountry> CN = get("cn");
	Supplier<HubCountry> CX = get("cx");
	Supplier<HubCountry> CC = get("cc");
	Supplier<HubCountry> CO = get("co");
	Supplier<HubCountry> KM = get("km");
	Supplier<HubCountry> CK = get("ck");
	Supplier<HubCountry> CR = get("cr");
	Supplier<HubCountry> HR = get("hr");
	Supplier<HubCountry> CU = get("cu");
	Supplier<HubCountry> CW = get("cw");
	Supplier<HubCountry> CY = get("cy");
	Supplier<HubCountry> CZ = get("cz");
	Supplier<HubCountry> DK = get("dk");
	Supplier<HubCountry> DJ = get("dj");
	Supplier<HubCountry> DM = get("dm");
	Supplier<HubCountry> DO = get("do");
	Supplier<HubCountry> CD = get("cd");
	Supplier<HubCountry> EC = get("ec");
	Supplier<HubCountry> EG = get("eg");
	Supplier<HubCountry> SV = get("sv");
	Supplier<HubCountry> GQ = get("gq");
	Supplier<HubCountry> ER = get("er");
	Supplier<HubCountry> EE = get("ee");
	Supplier<HubCountry> SZ = get("sz");
	Supplier<HubCountry> ET = get("et");
	Supplier<HubCountry> FK = get("fk");
	Supplier<HubCountry> FO = get("fo");
	Supplier<HubCountry> FJ = get("fj");
	Supplier<HubCountry> FI = get("fi");
	Supplier<HubCountry> FR = get("fr");
	Supplier<HubCountry> GF = get("gf");
	Supplier<HubCountry> PF = get("pf");
	Supplier<HubCountry> TF = get("tf");
	Supplier<HubCountry> GA = get("ga");
	Supplier<HubCountry> GM = get("gm");
	Supplier<HubCountry> GE = get("ge");
	Supplier<HubCountry> DE = get("de");
	Supplier<HubCountry> GH = get("gh");
	Supplier<HubCountry> GI = get("gi");
	Supplier<HubCountry> GR = get("gr");
	Supplier<HubCountry> GL = get("gl");
	Supplier<HubCountry> GD = get("gd");
	Supplier<HubCountry> GP = get("gp");
	Supplier<HubCountry> GU = get("gu");
	Supplier<HubCountry> GT = get("gt");
	Supplier<HubCountry> GG = get("gg");
	Supplier<HubCountry> GN = get("gn");
	Supplier<HubCountry> GW = get("gw");
	Supplier<HubCountry> GY = get("gy");
	Supplier<HubCountry> HT = get("ht");
	Supplier<HubCountry> HM = get("hm");
	Supplier<HubCountry> HN = get("hn");
	Supplier<HubCountry> HK = get("hk");
	Supplier<HubCountry> HU = get("hu");
	Supplier<HubCountry> IS = get("is");
	Supplier<HubCountry> IN = get("in");
	Supplier<HubCountry> ID = get("id");
	Supplier<HubCountry> IR = get("ir");
	Supplier<HubCountry> IQ = get("iq");
	Supplier<HubCountry> IE = get("ie");
	Supplier<HubCountry> IM = get("im");
	Supplier<HubCountry> IL = get("il");
	Supplier<HubCountry> IT = get("it");
	Supplier<HubCountry> CI = get("ci");
	Supplier<HubCountry> JM = get("jm");
	Supplier<HubCountry> JP = get("jp");
	Supplier<HubCountry> JE = get("je");
	Supplier<HubCountry> JO = get("jo");
	Supplier<HubCountry> KZ = get("kz");
	Supplier<HubCountry> KE = get("ke");
	Supplier<HubCountry> KI = get("ki");
	Supplier<HubCountry> XK = get("xk");
	Supplier<HubCountry> KW = get("kw");
	Supplier<HubCountry> KG = get("kg");
	Supplier<HubCountry> LA = get("la");
	Supplier<HubCountry> LV = get("lv");
	Supplier<HubCountry> LB = get("lb");
	Supplier<HubCountry> LS = get("ls");
	Supplier<HubCountry> LR = get("lr");
	Supplier<HubCountry> LY = get("ly");
	Supplier<HubCountry> LI = get("li");
	Supplier<HubCountry> LT = get("lt");
	Supplier<HubCountry> LU = get("lu");
	Supplier<HubCountry> MO = get("mo");
	Supplier<HubCountry> MG = get("mg");
	Supplier<HubCountry> MW = get("mw");
	Supplier<HubCountry> MY = get("my");
	Supplier<HubCountry> MV = get("mv");
	Supplier<HubCountry> ML = get("ml");
	Supplier<HubCountry> MT = get("mt");
	Supplier<HubCountry> MH = get("mh");
	Supplier<HubCountry> MQ = get("mq");
	Supplier<HubCountry> MR = get("mr");
	Supplier<HubCountry> MU = get("mu");
	Supplier<HubCountry> YT = get("yt");
	Supplier<HubCountry> MX = get("mx");
	Supplier<HubCountry> FM = get("fm");
	Supplier<HubCountry> MD = get("md");
	Supplier<HubCountry> MC = get("mc");
	Supplier<HubCountry> MN = get("mn");
	Supplier<HubCountry> ME = get("me");
	Supplier<HubCountry> MS = get("ms");
	Supplier<HubCountry> MA = get("ma");
	Supplier<HubCountry> MZ = get("mz");
	Supplier<HubCountry> MM = get("mm");
	Supplier<HubCountry> NA = get("na");
	Supplier<HubCountry> NR = get("nr");
	Supplier<HubCountry> NP = get("np");
	Supplier<HubCountry> NL = get("nl");
	Supplier<HubCountry> NC = get("nc");
	Supplier<HubCountry> NZ = get("nz");
	Supplier<HubCountry> NI = get("ni");
	Supplier<HubCountry> NE = get("ne");
	Supplier<HubCountry> NG = get("ng");
	Supplier<HubCountry> NU = get("nu");
	Supplier<HubCountry> NF = get("nf");
	Supplier<HubCountry> KP = get("kp");
	Supplier<HubCountry> MK = get("mk");
	Supplier<HubCountry> MP = get("mp");
	Supplier<HubCountry> NO = get("no");
	Supplier<HubCountry> OM = get("om");
	Supplier<HubCountry> PK = get("pk");
	Supplier<HubCountry> PW = get("pw");
	Supplier<HubCountry> PS = get("ps");
	Supplier<HubCountry> PA = get("pa");
	Supplier<HubCountry> PG = get("pg");
	Supplier<HubCountry> PY = get("py");
	Supplier<HubCountry> PE = get("pe");
	Supplier<HubCountry> PH = get("ph");
	Supplier<HubCountry> PN = get("pn");
	Supplier<HubCountry> PL = get("pl");
	Supplier<HubCountry> PT = get("pt");
	Supplier<HubCountry> PR = get("pr");
	Supplier<HubCountry> QA = get("qa");
	Supplier<HubCountry> CG = get("cg");
	Supplier<HubCountry> RO = get("ro");
	Supplier<HubCountry> RU = get("ru");
	Supplier<HubCountry> RW = get("rw");
	Supplier<HubCountry> RE = get("re");
	Supplier<HubCountry> BL = get("bl");
	Supplier<HubCountry> SH = get("sh");
	Supplier<HubCountry> KN = get("kn");
	Supplier<HubCountry> LC = get("lc");
	Supplier<HubCountry> MF = get("mf");
	Supplier<HubCountry> PM = get("pm");
	Supplier<HubCountry> VC = get("vc");
	Supplier<HubCountry> WS = get("ws");
	Supplier<HubCountry> SM = get("sm");
	Supplier<HubCountry> SA = get("sa");
	Supplier<HubCountry> SN = get("sn");
	Supplier<HubCountry> RS = get("rs");
	Supplier<HubCountry> SC = get("sc");
	Supplier<HubCountry> SL = get("sl");
	Supplier<HubCountry> SG = get("sg");
	Supplier<HubCountry> SX = get("sx");
	Supplier<HubCountry> SK = get("sk");
	Supplier<HubCountry> SI = get("si");
	Supplier<HubCountry> SB = get("sb");
	Supplier<HubCountry> SO = get("so");
	Supplier<HubCountry> ZA = get("za");
	Supplier<HubCountry> GS = get("gs");
	Supplier<HubCountry> KR = get("kr");
	Supplier<HubCountry> SS = get("ss");
	Supplier<HubCountry> ES = get("es");
	Supplier<HubCountry> LK = get("lk");
	Supplier<HubCountry> SD = get("sd");
	Supplier<HubCountry> SR = get("sr");
	Supplier<HubCountry> SJ = get("sj");
	Supplier<HubCountry> SE = get("se");
	Supplier<HubCountry> CH = get("ch");
	Supplier<HubCountry> SY = get("sy");
	Supplier<HubCountry> ST = get("st");
	Supplier<HubCountry> TW = get("tw");
	Supplier<HubCountry> TJ = get("tj");
	Supplier<HubCountry> TZ = get("tz");
	Supplier<HubCountry> TH = get("th");
	Supplier<HubCountry> TL = get("tl");
	Supplier<HubCountry> TG = get("tg");
	Supplier<HubCountry> TK = get("tk");
	Supplier<HubCountry> TO = get("to");
	Supplier<HubCountry> TT = get("tt");
	Supplier<HubCountry> TN = get("tn");
	Supplier<HubCountry> TR = get("tr");
	Supplier<HubCountry> TM = get("tm");
	Supplier<HubCountry> TC = get("tc");
	Supplier<HubCountry> TV = get("tv");
	Supplier<HubCountry> UG = get("ug");
	Supplier<HubCountry> UA = get("ua");
	Supplier<HubCountry> AE = get("ae");
	Supplier<HubCountry> GB = get("gb");
	Supplier<HubCountry> US = get("us");
	Supplier<HubCountry> UM = get("um");
	Supplier<HubCountry> VI = get("vi");
	Supplier<HubCountry> UY = get("uy");
	Supplier<HubCountry> UZ = get("uz");
	Supplier<HubCountry> VU = get("vu");
	Supplier<HubCountry> VA = get("va");
	Supplier<HubCountry> VE = get("ve");
	Supplier<HubCountry> VN = get("vn");
	Supplier<HubCountry> XW = get("xw");
	Supplier<HubCountry> WF = get("wf");
	Supplier<HubCountry> EH = get("eh");
	Supplier<HubCountry> YE = get("ye");
	Supplier<HubCountry> ZM = get("zm");
	Supplier<HubCountry> ZW = get("zw");
	Supplier<HubCountry> AX = get("ax");
}
