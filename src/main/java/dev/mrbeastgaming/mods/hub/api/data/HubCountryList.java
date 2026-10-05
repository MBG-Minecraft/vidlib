package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.util.Hex32;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record HubCountryList(
	Int2ObjectMap<HubCountry> byId,
	Map<String, HubCountry> byCode,
	Map<String, HubCountry> byCCA2,
	List<HubCountry> all
) {
	public static final HubCountryList EMPTY = new HubCountryList(Int2ObjectMaps.emptyMap(), Map.of(), Map.of(), List.of());

	public static HubCountryList of(Collection<HubCountry> list) {
		if (list.isEmpty()) {
			return EMPTY;
		}

		var byId = new Int2ObjectLinkedOpenHashMap<HubCountry>(list.size());
		var byCode = new Object2ObjectLinkedOpenHashMap<String, HubCountry>(list.size());
		var byCCA2 = new Object2ObjectLinkedOpenHashMap<String, HubCountry>(list.size());

		for (var country : list) {
			byId.put(country.id().raw(), country);
			byCode.put(country.code(), country);
			byCCA2.put(country.cca2(), country);
		}

		return new HubCountryList(byId, byCode, byCCA2, List.copyOf(list));
	}

	public static final Codec<HubCountryList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubCountry.DIRECT_CODEC.listOf().optionalFieldOf("countries", List.of()).forGetter(HubCountryList::all)
	).apply(instance, HubCountryList::of));

	public static final StreamCodec<ByteBuf, HubCountryList> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.listOf(HubCountry.STREAM_CODEC), HubCountryList::all,
		HubCountryList::of
	);

	public HubCountry getByCode(String code) {
		return byCode.computeIfAbsent(code, c -> new HubCountry(
			Hex32.NONE,
			c,
			true,
			c.toUpperCase(Locale.ROOT),
			"",
			0,
			c,
			c,
			c,
			""
		));
	}

	public HubCountry getAF() {
		return getByCode("af");
	}

	public HubCountry getAL() {
		return getByCode("al");
	}

	public HubCountry getDZ() {
		return getByCode("dz");
	}

	public HubCountry getAS() {
		return getByCode("as");
	}

	public HubCountry getAD() {
		return getByCode("ad");
	}

	public HubCountry getAO() {
		return getByCode("ao");
	}

	public HubCountry getAI() {
		return getByCode("ai");
	}

	public HubCountry getAQ() {
		return getByCode("aq");
	}

	public HubCountry getAG() {
		return getByCode("ag");
	}

	public HubCountry getAR() {
		return getByCode("ar");
	}

	public HubCountry getAM() {
		return getByCode("am");
	}

	public HubCountry getAW() {
		return getByCode("aw");
	}

	public HubCountry getAU() {
		return getByCode("au");
	}

	public HubCountry getAT() {
		return getByCode("at");
	}

	public HubCountry getAZ() {
		return getByCode("az");
	}

	public HubCountry getBS() {
		return getByCode("bs");
	}

	public HubCountry getBH() {
		return getByCode("bh");
	}

	public HubCountry getBD() {
		return getByCode("bd");
	}

	public HubCountry getBB() {
		return getByCode("bb");
	}

	public HubCountry getBY() {
		return getByCode("by");
	}

	public HubCountry getBE() {
		return getByCode("be");
	}

	public HubCountry getBZ() {
		return getByCode("bz");
	}

	public HubCountry getBJ() {
		return getByCode("bj");
	}

	public HubCountry getBM() {
		return getByCode("bm");
	}

	public HubCountry getBT() {
		return getByCode("bt");
	}

	public HubCountry getBO() {
		return getByCode("bo");
	}

	public HubCountry getBA() {
		return getByCode("ba");
	}

	public HubCountry getBW() {
		return getByCode("bw");
	}

	public HubCountry getBV() {
		return getByCode("bv");
	}

	public HubCountry getBR() {
		return getByCode("br");
	}

	public HubCountry getIO() {
		return getByCode("io");
	}

	public HubCountry getVG() {
		return getByCode("vg");
	}

	public HubCountry getBN() {
		return getByCode("bn");
	}

	public HubCountry getBG() {
		return getByCode("bg");
	}

	public HubCountry getBF() {
		return getByCode("bf");
	}

	public HubCountry getBI() {
		return getByCode("bi");
	}

	public HubCountry getKH() {
		return getByCode("kh");
	}

	public HubCountry getCM() {
		return getByCode("cm");
	}

	public HubCountry getCA() {
		return getByCode("ca");
	}

	public HubCountry getCV() {
		return getByCode("cv");
	}

	public HubCountry getBQ() {
		return getByCode("bq");
	}

	public HubCountry getKY() {
		return getByCode("ky");
	}

	public HubCountry getCF() {
		return getByCode("cf");
	}

	public HubCountry getTD() {
		return getByCode("td");
	}

	public HubCountry getCL() {
		return getByCode("cl");
	}

	public HubCountry getCN() {
		return getByCode("cn");
	}

	public HubCountry getCX() {
		return getByCode("cx");
	}

	public HubCountry getCC() {
		return getByCode("cc");
	}

	public HubCountry getCO() {
		return getByCode("co");
	}

	public HubCountry getKM() {
		return getByCode("km");
	}

	public HubCountry getCK() {
		return getByCode("ck");
	}

	public HubCountry getCR() {
		return getByCode("cr");
	}

	public HubCountry getHR() {
		return getByCode("hr");
	}

	public HubCountry getCU() {
		return getByCode("cu");
	}

	public HubCountry getCW() {
		return getByCode("cw");
	}

	public HubCountry getCY() {
		return getByCode("cy");
	}

	public HubCountry getCZ() {
		return getByCode("cz");
	}

	public HubCountry getDK() {
		return getByCode("dk");
	}

	public HubCountry getDJ() {
		return getByCode("dj");
	}

	public HubCountry getDM() {
		return getByCode("dm");
	}

	public HubCountry getDO() {
		return getByCode("do");
	}

	public HubCountry getCD() {
		return getByCode("cd");
	}

	public HubCountry getEC() {
		return getByCode("ec");
	}

	public HubCountry getEG() {
		return getByCode("eg");
	}

	public HubCountry getSV() {
		return getByCode("sv");
	}

	public HubCountry getGQ() {
		return getByCode("gq");
	}

	public HubCountry getER() {
		return getByCode("er");
	}

	public HubCountry getEE() {
		return getByCode("ee");
	}

	public HubCountry getSZ() {
		return getByCode("sz");
	}

	public HubCountry getET() {
		return getByCode("et");
	}

	public HubCountry getFK() {
		return getByCode("fk");
	}

	public HubCountry getFO() {
		return getByCode("fo");
	}

	public HubCountry getFJ() {
		return getByCode("fj");
	}

	public HubCountry getFI() {
		return getByCode("fi");
	}

	public HubCountry getFR() {
		return getByCode("fr");
	}

	public HubCountry getGF() {
		return getByCode("gf");
	}

	public HubCountry getPF() {
		return getByCode("pf");
	}

	public HubCountry getTF() {
		return getByCode("tf");
	}

	public HubCountry getGA() {
		return getByCode("ga");
	}

	public HubCountry getGM() {
		return getByCode("gm");
	}

	public HubCountry getGE() {
		return getByCode("ge");
	}

	public HubCountry getDE() {
		return getByCode("de");
	}

	public HubCountry getGH() {
		return getByCode("gh");
	}

	public HubCountry getGI() {
		return getByCode("gi");
	}

	public HubCountry getGR() {
		return getByCode("gr");
	}

	public HubCountry getGL() {
		return getByCode("gl");
	}

	public HubCountry getGD() {
		return getByCode("gd");
	}

	public HubCountry getGP() {
		return getByCode("gp");
	}

	public HubCountry getGU() {
		return getByCode("gu");
	}

	public HubCountry getGT() {
		return getByCode("gt");
	}

	public HubCountry getGG() {
		return getByCode("gg");
	}

	public HubCountry getGN() {
		return getByCode("gn");
	}

	public HubCountry getGW() {
		return getByCode("gw");
	}

	public HubCountry getGY() {
		return getByCode("gy");
	}

	public HubCountry getHT() {
		return getByCode("ht");
	}

	public HubCountry getHM() {
		return getByCode("hm");
	}

	public HubCountry getHN() {
		return getByCode("hn");
	}

	public HubCountry getHK() {
		return getByCode("hk");
	}

	public HubCountry getHU() {
		return getByCode("hu");
	}

	public HubCountry getIS() {
		return getByCode("is");
	}

	public HubCountry getIN() {
		return getByCode("in");
	}

	public HubCountry getID() {
		return getByCode("id");
	}

	public HubCountry getIR() {
		return getByCode("ir");
	}

	public HubCountry getIQ() {
		return getByCode("iq");
	}

	public HubCountry getIE() {
		return getByCode("ie");
	}

	public HubCountry getIM() {
		return getByCode("im");
	}

	public HubCountry getIL() {
		return getByCode("il");
	}

	public HubCountry getIT() {
		return getByCode("it");
	}

	public HubCountry getCI() {
		return getByCode("ci");
	}

	public HubCountry getJM() {
		return getByCode("jm");
	}

	public HubCountry getJP() {
		return getByCode("jp");
	}

	public HubCountry getJE() {
		return getByCode("je");
	}

	public HubCountry getJO() {
		return getByCode("jo");
	}

	public HubCountry getKZ() {
		return getByCode("kz");
	}

	public HubCountry getKE() {
		return getByCode("ke");
	}

	public HubCountry getKI() {
		return getByCode("ki");
	}

	public HubCountry getXK() {
		return getByCode("xk");
	}

	public HubCountry getKW() {
		return getByCode("kw");
	}

	public HubCountry getKG() {
		return getByCode("kg");
	}

	public HubCountry getLA() {
		return getByCode("la");
	}

	public HubCountry getLV() {
		return getByCode("lv");
	}

	public HubCountry getLB() {
		return getByCode("lb");
	}

	public HubCountry getLS() {
		return getByCode("ls");
	}

	public HubCountry getLR() {
		return getByCode("lr");
	}

	public HubCountry getLY() {
		return getByCode("ly");
	}

	public HubCountry getLI() {
		return getByCode("li");
	}

	public HubCountry getLT() {
		return getByCode("lt");
	}

	public HubCountry getLU() {
		return getByCode("lu");
	}

	public HubCountry getMO() {
		return getByCode("mo");
	}

	public HubCountry getMG() {
		return getByCode("mg");
	}

	public HubCountry getMW() {
		return getByCode("mw");
	}

	public HubCountry getMY() {
		return getByCode("my");
	}

	public HubCountry getMV() {
		return getByCode("mv");
	}

	public HubCountry getML() {
		return getByCode("ml");
	}

	public HubCountry getMT() {
		return getByCode("mt");
	}

	public HubCountry getMH() {
		return getByCode("mh");
	}

	public HubCountry getMQ() {
		return getByCode("mq");
	}

	public HubCountry getMR() {
		return getByCode("mr");
	}

	public HubCountry getMU() {
		return getByCode("mu");
	}

	public HubCountry getYT() {
		return getByCode("yt");
	}

	public HubCountry getMX() {
		return getByCode("mx");
	}

	public HubCountry getFM() {
		return getByCode("fm");
	}

	public HubCountry getMD() {
		return getByCode("md");
	}

	public HubCountry getMC() {
		return getByCode("mc");
	}

	public HubCountry getMN() {
		return getByCode("mn");
	}

	public HubCountry getME() {
		return getByCode("me");
	}

	public HubCountry getMS() {
		return getByCode("ms");
	}

	public HubCountry getMA() {
		return getByCode("ma");
	}

	public HubCountry getMZ() {
		return getByCode("mz");
	}

	public HubCountry getMM() {
		return getByCode("mm");
	}

	public HubCountry getNA() {
		return getByCode("na");
	}

	public HubCountry getNR() {
		return getByCode("nr");
	}

	public HubCountry getNP() {
		return getByCode("np");
	}

	public HubCountry getNL() {
		return getByCode("nl");
	}

	public HubCountry getNC() {
		return getByCode("nc");
	}

	public HubCountry getNZ() {
		return getByCode("nz");
	}

	public HubCountry getNI() {
		return getByCode("ni");
	}

	public HubCountry getNE() {
		return getByCode("ne");
	}

	public HubCountry getNG() {
		return getByCode("ng");
	}

	public HubCountry getNU() {
		return getByCode("nu");
	}

	public HubCountry getNF() {
		return getByCode("nf");
	}

	public HubCountry getKP() {
		return getByCode("kp");
	}

	public HubCountry getMK() {
		return getByCode("mk");
	}

	public HubCountry getMP() {
		return getByCode("mp");
	}

	public HubCountry getNO() {
		return getByCode("no");
	}

	public HubCountry getOM() {
		return getByCode("om");
	}

	public HubCountry getPK() {
		return getByCode("pk");
	}

	public HubCountry getPW() {
		return getByCode("pw");
	}

	public HubCountry getPS() {
		return getByCode("ps");
	}

	public HubCountry getPA() {
		return getByCode("pa");
	}

	public HubCountry getPG() {
		return getByCode("pg");
	}

	public HubCountry getPY() {
		return getByCode("py");
	}

	public HubCountry getPE() {
		return getByCode("pe");
	}

	public HubCountry getPH() {
		return getByCode("ph");
	}

	public HubCountry getPN() {
		return getByCode("pn");
	}

	public HubCountry getPL() {
		return getByCode("pl");
	}

	public HubCountry getPT() {
		return getByCode("pt");
	}

	public HubCountry getPR() {
		return getByCode("pr");
	}

	public HubCountry getQA() {
		return getByCode("qa");
	}

	public HubCountry getCG() {
		return getByCode("cg");
	}

	public HubCountry getRO() {
		return getByCode("ro");
	}

	public HubCountry getRU() {
		return getByCode("ru");
	}

	public HubCountry getRW() {
		return getByCode("rw");
	}

	public HubCountry getRE() {
		return getByCode("re");
	}

	public HubCountry getBL() {
		return getByCode("bl");
	}

	public HubCountry getSH() {
		return getByCode("sh");
	}

	public HubCountry getKN() {
		return getByCode("kn");
	}

	public HubCountry getLC() {
		return getByCode("lc");
	}

	public HubCountry getMF() {
		return getByCode("mf");
	}

	public HubCountry getPM() {
		return getByCode("pm");
	}

	public HubCountry getVC() {
		return getByCode("vc");
	}

	public HubCountry getWS() {
		return getByCode("ws");
	}

	public HubCountry getSM() {
		return getByCode("sm");
	}

	public HubCountry getSA() {
		return getByCode("sa");
	}

	public HubCountry getSN() {
		return getByCode("sn");
	}

	public HubCountry getRS() {
		return getByCode("rs");
	}

	public HubCountry getSC() {
		return getByCode("sc");
	}

	public HubCountry getSL() {
		return getByCode("sl");
	}

	public HubCountry getSG() {
		return getByCode("sg");
	}

	public HubCountry getSX() {
		return getByCode("sx");
	}

	public HubCountry getSK() {
		return getByCode("sk");
	}

	public HubCountry getSI() {
		return getByCode("si");
	}

	public HubCountry getSB() {
		return getByCode("sb");
	}

	public HubCountry getSO() {
		return getByCode("so");
	}

	public HubCountry getZA() {
		return getByCode("za");
	}

	public HubCountry getGS() {
		return getByCode("gs");
	}

	public HubCountry getKR() {
		return getByCode("kr");
	}

	public HubCountry getSS() {
		return getByCode("ss");
	}

	public HubCountry getES() {
		return getByCode("es");
	}

	public HubCountry getLK() {
		return getByCode("lk");
	}

	public HubCountry getSD() {
		return getByCode("sd");
	}

	public HubCountry getSR() {
		return getByCode("sr");
	}

	public HubCountry getSJ() {
		return getByCode("sj");
	}

	public HubCountry getSE() {
		return getByCode("se");
	}

	public HubCountry getCH() {
		return getByCode("ch");
	}

	public HubCountry getSY() {
		return getByCode("sy");
	}

	public HubCountry getST() {
		return getByCode("st");
	}

	public HubCountry getTW() {
		return getByCode("tw");
	}

	public HubCountry getTJ() {
		return getByCode("tj");
	}

	public HubCountry getTZ() {
		return getByCode("tz");
	}

	public HubCountry getTH() {
		return getByCode("th");
	}

	public HubCountry getTL() {
		return getByCode("tl");
	}

	public HubCountry getTG() {
		return getByCode("tg");
	}

	public HubCountry getTK() {
		return getByCode("tk");
	}

	public HubCountry getTO() {
		return getByCode("to");
	}

	public HubCountry getTT() {
		return getByCode("tt");
	}

	public HubCountry getTN() {
		return getByCode("tn");
	}

	public HubCountry getTR() {
		return getByCode("tr");
	}

	public HubCountry getTM() {
		return getByCode("tm");
	}

	public HubCountry getTC() {
		return getByCode("tc");
	}

	public HubCountry getTV() {
		return getByCode("tv");
	}

	public HubCountry getUG() {
		return getByCode("ug");
	}

	public HubCountry getUA() {
		return getByCode("ua");
	}

	public HubCountry getAE() {
		return getByCode("ae");
	}

	public HubCountry getGB() {
		return getByCode("gb");
	}

	public HubCountry getUS() {
		return getByCode("us");
	}

	public HubCountry getUM() {
		return getByCode("um");
	}

	public HubCountry getVI() {
		return getByCode("vi");
	}

	public HubCountry getUY() {
		return getByCode("uy");
	}

	public HubCountry getUZ() {
		return getByCode("uz");
	}

	public HubCountry getVU() {
		return getByCode("vu");
	}

	public HubCountry getVA() {
		return getByCode("va");
	}

	public HubCountry getVE() {
		return getByCode("ve");
	}

	public HubCountry getVN() {
		return getByCode("vn");
	}

	public HubCountry getXW() {
		return getByCode("xw");
	}

	public HubCountry getWF() {
		return getByCode("wf");
	}

	public HubCountry getEH() {
		return getByCode("eh");
	}

	public HubCountry getYE() {
		return getByCode("ye");
	}

	public HubCountry getZM() {
		return getByCode("zm");
	}

	public HubCountry getZW() {
		return getByCode("zw");
	}

	public HubCountry getAX() {
		return getByCode("ax");
	}
}
