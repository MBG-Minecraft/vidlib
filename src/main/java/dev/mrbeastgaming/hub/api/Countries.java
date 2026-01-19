package dev.mrbeastgaming.hub.api;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.neoforged.fml.loading.FMLLoader;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface Countries {
	Lazy<Path> LOCAL_PATH = CommonPaths.USER.<Path>map(path -> path.resolve("beast-hub/countries.json"));

	Lazy<CountryList> REMOTE = Lazy.of(() -> {
		try {
			var request = MiscUtils.HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create("https://hub.mrbeastgaming.dev/api/countries")).build(), HttpResponse.BodyHandlers.ofInputStream());
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

	List<Lazy<Country>> CACHED = new ArrayList<>();

	static Supplier<Country> add(String code) {
		var lazy = LOADED.<Country>map(map -> map.byCode().get(code));
		CACHED.add(lazy);
		return lazy;
	}

	Supplier<Country> AF = add("af");
	Supplier<Country> AL = add("al");
	Supplier<Country> DZ = add("dz");
	Supplier<Country> AS = add("as");
	Supplier<Country> AD = add("ad");
	Supplier<Country> AO = add("ao");
	Supplier<Country> AI = add("ai");
	Supplier<Country> AQ = add("aq");
	Supplier<Country> AG = add("ag");
	Supplier<Country> AR = add("ar");
	Supplier<Country> AM = add("am");
	Supplier<Country> AW = add("aw");
	Supplier<Country> AU = add("au");
	Supplier<Country> AT = add("at");
	Supplier<Country> AZ = add("az");
	Supplier<Country> BS = add("bs");
	Supplier<Country> BH = add("bh");
	Supplier<Country> BD = add("bd");
	Supplier<Country> BB = add("bb");
	Supplier<Country> BY = add("by");
	Supplier<Country> BE = add("be");
	Supplier<Country> BZ = add("bz");
	Supplier<Country> BJ = add("bj");
	Supplier<Country> BM = add("bm");
	Supplier<Country> BT = add("bt");
	Supplier<Country> BO = add("bo");
	Supplier<Country> BA = add("ba");
	Supplier<Country> BW = add("bw");
	Supplier<Country> BV = add("bv");
	Supplier<Country> BR = add("br");
	Supplier<Country> IO = add("io");
	Supplier<Country> VG = add("vg");
	Supplier<Country> BN = add("bn");
	Supplier<Country> BG = add("bg");
	Supplier<Country> BF = add("bf");
	Supplier<Country> BI = add("bi");
	Supplier<Country> KH = add("kh");
	Supplier<Country> CM = add("cm");
	Supplier<Country> CA = add("ca");
	Supplier<Country> CV = add("cv");
	Supplier<Country> BQ = add("bq");
	Supplier<Country> KY = add("ky");
	Supplier<Country> CF = add("cf");
	Supplier<Country> TD = add("td");
	Supplier<Country> CL = add("cl");
	Supplier<Country> CN = add("cn");
	Supplier<Country> CX = add("cx");
	Supplier<Country> CC = add("cc");
	Supplier<Country> CO = add("co");
	Supplier<Country> KM = add("km");
	Supplier<Country> CK = add("ck");
	Supplier<Country> CR = add("cr");
	Supplier<Country> HR = add("hr");
	Supplier<Country> CU = add("cu");
	Supplier<Country> CW = add("cw");
	Supplier<Country> CY = add("cy");
	Supplier<Country> CZ = add("cz");
	Supplier<Country> DK = add("dk");
	Supplier<Country> DJ = add("dj");
	Supplier<Country> DM = add("dm");
	Supplier<Country> DO = add("do");
	Supplier<Country> CD = add("cd");
	Supplier<Country> EC = add("ec");
	Supplier<Country> EG = add("eg");
	Supplier<Country> SV = add("sv");
	Supplier<Country> GQ = add("gq");
	Supplier<Country> ER = add("er");
	Supplier<Country> EE = add("ee");
	Supplier<Country> SZ = add("sz");
	Supplier<Country> ET = add("et");
	Supplier<Country> FK = add("fk");
	Supplier<Country> FO = add("fo");
	Supplier<Country> FJ = add("fj");
	Supplier<Country> FI = add("fi");
	Supplier<Country> FR = add("fr");
	Supplier<Country> GF = add("gf");
	Supplier<Country> PF = add("pf");
	Supplier<Country> TF = add("tf");
	Supplier<Country> GA = add("ga");
	Supplier<Country> GM = add("gm");
	Supplier<Country> GE = add("ge");
	Supplier<Country> DE = add("de");
	Supplier<Country> GH = add("gh");
	Supplier<Country> GI = add("gi");
	Supplier<Country> GR = add("gr");
	Supplier<Country> GL = add("gl");
	Supplier<Country> GD = add("gd");
	Supplier<Country> GP = add("gp");
	Supplier<Country> GU = add("gu");
	Supplier<Country> GT = add("gt");
	Supplier<Country> GG = add("gg");
	Supplier<Country> GN = add("gn");
	Supplier<Country> GW = add("gw");
	Supplier<Country> GY = add("gy");
	Supplier<Country> HT = add("ht");
	Supplier<Country> HM = add("hm");
	Supplier<Country> HN = add("hn");
	Supplier<Country> HK = add("hk");
	Supplier<Country> HU = add("hu");
	Supplier<Country> IS = add("is");
	Supplier<Country> IN = add("in");
	Supplier<Country> ID = add("id");
	Supplier<Country> IR = add("ir");
	Supplier<Country> IQ = add("iq");
	Supplier<Country> IE = add("ie");
	Supplier<Country> IM = add("im");
	Supplier<Country> IL = add("il");
	Supplier<Country> IT = add("it");
	Supplier<Country> CI = add("ci");
	Supplier<Country> JM = add("jm");
	Supplier<Country> JP = add("jp");
	Supplier<Country> JE = add("je");
	Supplier<Country> JO = add("jo");
	Supplier<Country> KZ = add("kz");
	Supplier<Country> KE = add("ke");
	Supplier<Country> KI = add("ki");
	Supplier<Country> XK = add("xk");
	Supplier<Country> KW = add("kw");
	Supplier<Country> KG = add("kg");
	Supplier<Country> LA = add("la");
	Supplier<Country> LV = add("lv");
	Supplier<Country> LB = add("lb");
	Supplier<Country> LS = add("ls");
	Supplier<Country> LR = add("lr");
	Supplier<Country> LY = add("ly");
	Supplier<Country> LI = add("li");
	Supplier<Country> LT = add("lt");
	Supplier<Country> LU = add("lu");
	Supplier<Country> MO = add("mo");
	Supplier<Country> MG = add("mg");
	Supplier<Country> MW = add("mw");
	Supplier<Country> MY = add("my");
	Supplier<Country> MV = add("mv");
	Supplier<Country> ML = add("ml");
	Supplier<Country> MT = add("mt");
	Supplier<Country> MH = add("mh");
	Supplier<Country> MQ = add("mq");
	Supplier<Country> MR = add("mr");
	Supplier<Country> MU = add("mu");
	Supplier<Country> YT = add("yt");
	Supplier<Country> MX = add("mx");
	Supplier<Country> FM = add("fm");
	Supplier<Country> MD = add("md");
	Supplier<Country> MC = add("mc");
	Supplier<Country> MN = add("mn");
	Supplier<Country> ME = add("me");
	Supplier<Country> MS = add("ms");
	Supplier<Country> MA = add("ma");
	Supplier<Country> MZ = add("mz");
	Supplier<Country> MM = add("mm");
	Supplier<Country> NA = add("na");
	Supplier<Country> NR = add("nr");
	Supplier<Country> NP = add("np");
	Supplier<Country> NL = add("nl");
	Supplier<Country> NC = add("nc");
	Supplier<Country> NZ = add("nz");
	Supplier<Country> NI = add("ni");
	Supplier<Country> NE = add("ne");
	Supplier<Country> NG = add("ng");
	Supplier<Country> NU = add("nu");
	Supplier<Country> NF = add("nf");
	Supplier<Country> KP = add("kp");
	Supplier<Country> MK = add("mk");
	Supplier<Country> MP = add("mp");
	Supplier<Country> NO = add("no");
	Supplier<Country> OM = add("om");
	Supplier<Country> PK = add("pk");
	Supplier<Country> PW = add("pw");
	Supplier<Country> PS = add("ps");
	Supplier<Country> PA = add("pa");
	Supplier<Country> PG = add("pg");
	Supplier<Country> PY = add("py");
	Supplier<Country> PE = add("pe");
	Supplier<Country> PH = add("ph");
	Supplier<Country> PN = add("pn");
	Supplier<Country> PL = add("pl");
	Supplier<Country> PT = add("pt");
	Supplier<Country> PR = add("pr");
	Supplier<Country> QA = add("qa");
	Supplier<Country> CG = add("cg");
	Supplier<Country> RO = add("ro");
	Supplier<Country> RU = add("ru");
	Supplier<Country> RW = add("rw");
	Supplier<Country> RE = add("re");
	Supplier<Country> BL = add("bl");
	Supplier<Country> SH = add("sh");
	Supplier<Country> KN = add("kn");
	Supplier<Country> LC = add("lc");
	Supplier<Country> MF = add("mf");
	Supplier<Country> PM = add("pm");
	Supplier<Country> VC = add("vc");
	Supplier<Country> WS = add("ws");
	Supplier<Country> SM = add("sm");
	Supplier<Country> SA = add("sa");
	Supplier<Country> SN = add("sn");
	Supplier<Country> RS = add("rs");
	Supplier<Country> SC = add("sc");
	Supplier<Country> SL = add("sl");
	Supplier<Country> SG = add("sg");
	Supplier<Country> SX = add("sx");
	Supplier<Country> SK = add("sk");
	Supplier<Country> SI = add("si");
	Supplier<Country> SB = add("sb");
	Supplier<Country> SO = add("so");
	Supplier<Country> ZA = add("za");
	Supplier<Country> GS = add("gs");
	Supplier<Country> KR = add("kr");
	Supplier<Country> SS = add("ss");
	Supplier<Country> ES = add("es");
	Supplier<Country> LK = add("lk");
	Supplier<Country> SD = add("sd");
	Supplier<Country> SR = add("sr");
	Supplier<Country> SJ = add("sj");
	Supplier<Country> SE = add("se");
	Supplier<Country> CH = add("ch");
	Supplier<Country> SY = add("sy");
	Supplier<Country> ST = add("st");
	Supplier<Country> TW = add("tw");
	Supplier<Country> TJ = add("tj");
	Supplier<Country> TZ = add("tz");
	Supplier<Country> TH = add("th");
	Supplier<Country> TL = add("tl");
	Supplier<Country> TG = add("tg");
	Supplier<Country> TK = add("tk");
	Supplier<Country> TO = add("to");
	Supplier<Country> TT = add("tt");
	Supplier<Country> TN = add("tn");
	Supplier<Country> TR = add("tr");
	Supplier<Country> TM = add("tm");
	Supplier<Country> TC = add("tc");
	Supplier<Country> TV = add("tv");
	Supplier<Country> UG = add("ug");
	Supplier<Country> UA = add("ua");
	Supplier<Country> AE = add("ae");
	Supplier<Country> GB = add("gb");
	Supplier<Country> US = add("us");
	Supplier<Country> UM = add("um");
	Supplier<Country> VI = add("vi");
	Supplier<Country> UY = add("uy");
	Supplier<Country> UZ = add("uz");
	Supplier<Country> VU = add("vu");
	Supplier<Country> VA = add("va");
	Supplier<Country> VE = add("ve");
	Supplier<Country> VN = add("vn");
	Supplier<Country> WF = add("wf");
	Supplier<Country> EH = add("eh");
	Supplier<Country> YE = add("ye");
	Supplier<Country> ZM = add("zm");
	Supplier<Country> ZW = add("zw");
	Supplier<Country> AX = add("ax");
}
