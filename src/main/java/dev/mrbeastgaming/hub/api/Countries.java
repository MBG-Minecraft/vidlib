package dev.mrbeastgaming.hub.api;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.JsonUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.neoforged.fml.loading.FMLLoader;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public interface Countries {
	Lazy<Path> LOCAL_PATH = CommonPaths.USER.<Path>map(path -> path.resolve("beast-hub/countries.json"));

	Lazy<CountryList> REMOTE = Lazy.of(() -> {
		try {
			var request = API.HTTP_CLIENT.send(API.request("/api/countries").build(), HttpResponse.BodyHandlers.ofInputStream());
			var checksum = request.headers().firstValue("X-Checksum").orElse("");

			if (!checksum.isEmpty()) {
				try (var in = request.body()) {
					var json = JsonUtils.read(in);
					var countryList = CountryList.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

					if (countryList != CountryList.EMPTY) {
						var path = CommonPaths.mkdirs(LOCAL_PATH.get());

						try (var writer = Files.newBufferedWriter(path)) {
							JsonUtils.write(writer, CountryList.CODEC.encodeStart(JsonOps.INSTANCE, countryList).getOrThrow(), false);
						}
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
			try (var reader = Files.newBufferedReader(path)) {
				var json = JsonUtils.read(reader);
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

	Map<String, Lazy<Country>> CACHE = new Object2ObjectOpenHashMap<>();

	static Supplier<Country> get(String code) {
		return CACHE.computeIfAbsent(code, c -> LOADED.<Country>map(map -> map.byCode().get(code)));
	}

	Supplier<Country> AF = get("af");
	Supplier<Country> AL = get("al");
	Supplier<Country> DZ = get("dz");
	Supplier<Country> AS = get("as");
	Supplier<Country> AD = get("ad");
	Supplier<Country> AO = get("ao");
	Supplier<Country> AI = get("ai");
	Supplier<Country> AQ = get("aq");
	Supplier<Country> AG = get("ag");
	Supplier<Country> AR = get("ar");
	Supplier<Country> AM = get("am");
	Supplier<Country> AW = get("aw");
	Supplier<Country> AU = get("au");
	Supplier<Country> AT = get("at");
	Supplier<Country> AZ = get("az");
	Supplier<Country> BS = get("bs");
	Supplier<Country> BH = get("bh");
	Supplier<Country> BD = get("bd");
	Supplier<Country> BB = get("bb");
	Supplier<Country> BY = get("by");
	Supplier<Country> BE = get("be");
	Supplier<Country> BZ = get("bz");
	Supplier<Country> BJ = get("bj");
	Supplier<Country> BM = get("bm");
	Supplier<Country> BT = get("bt");
	Supplier<Country> BO = get("bo");
	Supplier<Country> BA = get("ba");
	Supplier<Country> BW = get("bw");
	Supplier<Country> BV = get("bv");
	Supplier<Country> BR = get("br");
	Supplier<Country> IO = get("io");
	Supplier<Country> VG = get("vg");
	Supplier<Country> BN = get("bn");
	Supplier<Country> BG = get("bg");
	Supplier<Country> BF = get("bf");
	Supplier<Country> BI = get("bi");
	Supplier<Country> KH = get("kh");
	Supplier<Country> CM = get("cm");
	Supplier<Country> CA = get("ca");
	Supplier<Country> CV = get("cv");
	Supplier<Country> BQ = get("bq");
	Supplier<Country> KY = get("ky");
	Supplier<Country> CF = get("cf");
	Supplier<Country> TD = get("td");
	Supplier<Country> CL = get("cl");
	Supplier<Country> CN = get("cn");
	Supplier<Country> CX = get("cx");
	Supplier<Country> CC = get("cc");
	Supplier<Country> CO = get("co");
	Supplier<Country> KM = get("km");
	Supplier<Country> CK = get("ck");
	Supplier<Country> CR = get("cr");
	Supplier<Country> HR = get("hr");
	Supplier<Country> CU = get("cu");
	Supplier<Country> CW = get("cw");
	Supplier<Country> CY = get("cy");
	Supplier<Country> CZ = get("cz");
	Supplier<Country> DK = get("dk");
	Supplier<Country> DJ = get("dj");
	Supplier<Country> DM = get("dm");
	Supplier<Country> DO = get("do");
	Supplier<Country> CD = get("cd");
	Supplier<Country> EC = get("ec");
	Supplier<Country> EG = get("eg");
	Supplier<Country> SV = get("sv");
	Supplier<Country> GQ = get("gq");
	Supplier<Country> ER = get("er");
	Supplier<Country> EE = get("ee");
	Supplier<Country> SZ = get("sz");
	Supplier<Country> ET = get("et");
	Supplier<Country> FK = get("fk");
	Supplier<Country> FO = get("fo");
	Supplier<Country> FJ = get("fj");
	Supplier<Country> FI = get("fi");
	Supplier<Country> FR = get("fr");
	Supplier<Country> GF = get("gf");
	Supplier<Country> PF = get("pf");
	Supplier<Country> TF = get("tf");
	Supplier<Country> GA = get("ga");
	Supplier<Country> GM = get("gm");
	Supplier<Country> GE = get("ge");
	Supplier<Country> DE = get("de");
	Supplier<Country> GH = get("gh");
	Supplier<Country> GI = get("gi");
	Supplier<Country> GR = get("gr");
	Supplier<Country> GL = get("gl");
	Supplier<Country> GD = get("gd");
	Supplier<Country> GP = get("gp");
	Supplier<Country> GU = get("gu");
	Supplier<Country> GT = get("gt");
	Supplier<Country> GG = get("gg");
	Supplier<Country> GN = get("gn");
	Supplier<Country> GW = get("gw");
	Supplier<Country> GY = get("gy");
	Supplier<Country> HT = get("ht");
	Supplier<Country> HM = get("hm");
	Supplier<Country> HN = get("hn");
	Supplier<Country> HK = get("hk");
	Supplier<Country> HU = get("hu");
	Supplier<Country> IS = get("is");
	Supplier<Country> IN = get("in");
	Supplier<Country> ID = get("id");
	Supplier<Country> IR = get("ir");
	Supplier<Country> IQ = get("iq");
	Supplier<Country> IE = get("ie");
	Supplier<Country> IM = get("im");
	Supplier<Country> IL = get("il");
	Supplier<Country> IT = get("it");
	Supplier<Country> CI = get("ci");
	Supplier<Country> JM = get("jm");
	Supplier<Country> JP = get("jp");
	Supplier<Country> JE = get("je");
	Supplier<Country> JO = get("jo");
	Supplier<Country> KZ = get("kz");
	Supplier<Country> KE = get("ke");
	Supplier<Country> KI = get("ki");
	Supplier<Country> XK = get("xk");
	Supplier<Country> KW = get("kw");
	Supplier<Country> KG = get("kg");
	Supplier<Country> LA = get("la");
	Supplier<Country> LV = get("lv");
	Supplier<Country> LB = get("lb");
	Supplier<Country> LS = get("ls");
	Supplier<Country> LR = get("lr");
	Supplier<Country> LY = get("ly");
	Supplier<Country> LI = get("li");
	Supplier<Country> LT = get("lt");
	Supplier<Country> LU = get("lu");
	Supplier<Country> MO = get("mo");
	Supplier<Country> MG = get("mg");
	Supplier<Country> MW = get("mw");
	Supplier<Country> MY = get("my");
	Supplier<Country> MV = get("mv");
	Supplier<Country> ML = get("ml");
	Supplier<Country> MT = get("mt");
	Supplier<Country> MH = get("mh");
	Supplier<Country> MQ = get("mq");
	Supplier<Country> MR = get("mr");
	Supplier<Country> MU = get("mu");
	Supplier<Country> YT = get("yt");
	Supplier<Country> MX = get("mx");
	Supplier<Country> FM = get("fm");
	Supplier<Country> MD = get("md");
	Supplier<Country> MC = get("mc");
	Supplier<Country> MN = get("mn");
	Supplier<Country> ME = get("me");
	Supplier<Country> MS = get("ms");
	Supplier<Country> MA = get("ma");
	Supplier<Country> MZ = get("mz");
	Supplier<Country> MM = get("mm");
	Supplier<Country> NA = get("na");
	Supplier<Country> NR = get("nr");
	Supplier<Country> NP = get("np");
	Supplier<Country> NL = get("nl");
	Supplier<Country> NC = get("nc");
	Supplier<Country> NZ = get("nz");
	Supplier<Country> NI = get("ni");
	Supplier<Country> NE = get("ne");
	Supplier<Country> NG = get("ng");
	Supplier<Country> NU = get("nu");
	Supplier<Country> NF = get("nf");
	Supplier<Country> KP = get("kp");
	Supplier<Country> MK = get("mk");
	Supplier<Country> MP = get("mp");
	Supplier<Country> NO = get("no");
	Supplier<Country> OM = get("om");
	Supplier<Country> PK = get("pk");
	Supplier<Country> PW = get("pw");
	Supplier<Country> PS = get("ps");
	Supplier<Country> PA = get("pa");
	Supplier<Country> PG = get("pg");
	Supplier<Country> PY = get("py");
	Supplier<Country> PE = get("pe");
	Supplier<Country> PH = get("ph");
	Supplier<Country> PN = get("pn");
	Supplier<Country> PL = get("pl");
	Supplier<Country> PT = get("pt");
	Supplier<Country> PR = get("pr");
	Supplier<Country> QA = get("qa");
	Supplier<Country> CG = get("cg");
	Supplier<Country> RO = get("ro");
	Supplier<Country> RU = get("ru");
	Supplier<Country> RW = get("rw");
	Supplier<Country> RE = get("re");
	Supplier<Country> BL = get("bl");
	Supplier<Country> SH = get("sh");
	Supplier<Country> KN = get("kn");
	Supplier<Country> LC = get("lc");
	Supplier<Country> MF = get("mf");
	Supplier<Country> PM = get("pm");
	Supplier<Country> VC = get("vc");
	Supplier<Country> WS = get("ws");
	Supplier<Country> SM = get("sm");
	Supplier<Country> SA = get("sa");
	Supplier<Country> SN = get("sn");
	Supplier<Country> RS = get("rs");
	Supplier<Country> SC = get("sc");
	Supplier<Country> SL = get("sl");
	Supplier<Country> SG = get("sg");
	Supplier<Country> SX = get("sx");
	Supplier<Country> SK = get("sk");
	Supplier<Country> SI = get("si");
	Supplier<Country> SB = get("sb");
	Supplier<Country> SO = get("so");
	Supplier<Country> ZA = get("za");
	Supplier<Country> GS = get("gs");
	Supplier<Country> KR = get("kr");
	Supplier<Country> SS = get("ss");
	Supplier<Country> ES = get("es");
	Supplier<Country> LK = get("lk");
	Supplier<Country> SD = get("sd");
	Supplier<Country> SR = get("sr");
	Supplier<Country> SJ = get("sj");
	Supplier<Country> SE = get("se");
	Supplier<Country> CH = get("ch");
	Supplier<Country> SY = get("sy");
	Supplier<Country> ST = get("st");
	Supplier<Country> TW = get("tw");
	Supplier<Country> TJ = get("tj");
	Supplier<Country> TZ = get("tz");
	Supplier<Country> TH = get("th");
	Supplier<Country> TL = get("tl");
	Supplier<Country> TG = get("tg");
	Supplier<Country> TK = get("tk");
	Supplier<Country> TO = get("to");
	Supplier<Country> TT = get("tt");
	Supplier<Country> TN = get("tn");
	Supplier<Country> TR = get("tr");
	Supplier<Country> TM = get("tm");
	Supplier<Country> TC = get("tc");
	Supplier<Country> TV = get("tv");
	Supplier<Country> UG = get("ug");
	Supplier<Country> UA = get("ua");
	Supplier<Country> AE = get("ae");
	Supplier<Country> GB = get("gb");
	Supplier<Country> US = get("us");
	Supplier<Country> UM = get("um");
	Supplier<Country> VI = get("vi");
	Supplier<Country> UY = get("uy");
	Supplier<Country> UZ = get("uz");
	Supplier<Country> VU = get("vu");
	Supplier<Country> VA = get("va");
	Supplier<Country> VE = get("ve");
	Supplier<Country> VN = get("vn");
	Supplier<Country> WF = get("wf");
	Supplier<Country> EH = get("eh");
	Supplier<Country> YE = get("ye");
	Supplier<Country> ZM = get("zm");
	Supplier<Country> ZW = get("zw");
	Supplier<Country> AX = get("ax");
}
