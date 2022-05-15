package com.soywiz.korge3d

import com.soywiz.kds.FastStringMap
import com.soywiz.kds.get
import com.soywiz.kds.keys
import com.soywiz.kds.size
import com.soywiz.korge3d.format.ColladaParser
import com.soywiz.korge3d.format.readColladaLibrary
import com.soywiz.korio.async.suspendTestNoJs
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.serialization.xml.toXml
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Korge3DExperimental
class ColladaTest {
	@Test
	fun testParseSourcesFloat() = suspendTestNoJs {
		val sources = ColladaParser().parseSources(
			"""<xml>
				<source id="Cube-mesh-positions">
				  <float_array id="Cube-mesh-positions-array" count="24">1 1 -1 1 -1 -1 -1 -0.9999998 -1 -0.9999997 1 -1 1 0.9999995 1 0.9999994 -1.000001 1 -1 -0.9999997 1 -1 1 1</float_array>
				  <technique_common>
					<accessor source="#Cube-mesh-positions-array" count="8" stride="3">
					  <param name="X" type="float"/>
					  <param name="Y" type="float"/>
					  <param name="Z" type="float"/>
					</accessor>
				  </technique_common>
				</source>
			</xml>""".trimIndent().toXml(),
			FastStringMap() // @TODO: Is this making JS to fail?
		)
		assertEquals(1, sources.size)
		assertEquals("Cube-mesh-positions", sources.first().id)
		assertEquals("X,Y,Z", sources.first().params.keys.sorted().joinToString(","))
        assertContentEquals(
            floatArrayOf(1.0f, 1.0f, -1.0f, -0.9999997f, 1.0f, 0.9999994f, -1.0f, -1.0f),
            (sources.first().params["X"] as ColladaParser.FloatSourceParam).floats.toFloatArray()
        )
	}

	@Test
	fun testParseSourcesName() {
		val sources = ColladaParser().parseSources(
			"""<xml>
			  <source id="Armature_Bone_pose_matrix-interpolation">
				<Name_array id="Armature_Bone_pose_matrix-interpolation-array" count="2">LINEAR LINEAR</Name_array>
				<technique_common>
				  <accessor source="#Armature_Bone_pose_matrix-interpolation-array" count="2" stride="1">
					<param name="INTERPOLATION" type="name"/>
				  </accessor>
				</technique_common>
			  </source>
			</xml>""".trimIndent().toXml(),
			FastStringMap()
		)
		assertEquals(1, sources.size)
		assertEquals("Armature_Bone_pose_matrix-interpolation", sources.first().id)
		assertEquals("INTERPOLATION", sources.first().params.keys.sorted().joinToString(","))
		assertEquals("[LINEAR, LINEAR]", (sources.first().params["INTERPOLATION"] as ColladaParser.NamesSourceParam).names.toString())
	}

	@Test
	fun testParseControllers() {
		val skins = ColladaParser().parseControllers("""<xml>
			<library_controllers>
				<controller id="Armature_Cylinder-skin" name="Armature">
				  <skin source="#Cylinder-mesh">
					<bind_shape_matrix>1 0 0 0 0 1 0 0 0 0 1 1 0 0 0 1</bind_shape_matrix>
					<source id="Armature_Cylinder-skin-joints">
					  <Name_array id="Armature_Cylinder-skin-joints-array" count="4">Bone Bone_001 Bone_002 Bone_003</Name_array>
					  <technique_common>
						<accessor source="#Armature_Cylinder-skin-joints-array" count="4" stride="1">
						  <param name="JOINT" type="name"/>
						</accessor>
					  </technique_common>
					</source>
					<source id="Armature_Cylinder-skin-bind_poses">
					  <float_array id="Armature_Cylinder-skin-bind_poses-array" count="64">1 0 0 0 0 0 1 0 0 -1 0 0 0 0 0 1 1 0 0 0 0 0 1 -1 0 -1 0 0 0 0 0 1 1 0 0 0 0 0 1 -2 0 -1 0 0 0 0 0 1 1 0 0 0 0 0 1 -3 0 -1 0 0 0 0 0 1</float_array>
					  <technique_common>
						<accessor source="#Armature_Cylinder-skin-bind_poses-array" count="4" stride="16">
						  <param name="TRANSFORM" type="float4x4"/>
						</accessor>
					  </technique_common>
					</source>
					<source id="Armature_Cylinder-skin-weights">
					  <float_array id="Armature_Cylinder-skin-weights-array" count="960">0.7111548 0.2888453 0.1513611 0.8486388 0.695326 0.304674 0.1507177 0.8492822 0.6837596 0.3162404 0.1477674 0.8522326 0.6868516 0.3131484 0.143753 0.856247 0.6816977 0.3183023 0.1400071 0.8599929 0.6644041 0.3355959 0.1331707 0.8668293 0.6582818 0.3417182 0.1281771 0.8718228 0.6982944 0.3017056 0.1220462 0.8779538 0.730853 0.269147 0.1208163 0.8791838 0.7579215 0.2420785 0.1220241 0.877976 0.7807247 0.2192753 0.122967 0.877033 0.790926 0.209074 0.1249497 0.8750503 0.7943176 0.2056825 0.1292516 0.8707484 0.793571 0.2064289 0.1356438 0.8643562 0.7913764 0.2086235 0.1401223 0.8598777 0.7878948 0.2121051 0.1451844 0.8548155 0.783146 0.2168539 0.1487381 0.8512619 0.7862349 0.2137651 0.1497865 0.8502135 0.8063114 0.1936886 0.1485277 0.8514723 0.8190507 0.1809493 0.1462466 0.8537535 0.8248908 0.1751092 0.1418048 0.8581951 0.8256891 0.1743108 0.136087 0.8639131 0.8224142 0.1775858 0.1334559 0.866544 0.8147199 0.1852801 0.1341467 0.8658533 0.8006632 0.1993368 0.1314637 0.8685364 0.7791803 0.2208197 0.1288103 0.8711897 0.748411 0.251589 0.1299018 0.8700982 0.7137156 0.2862845 0.1308623 0.8691377 0.7120171 0.2879829 0.1352198 0.8647803 0.7219893 0.2780108 0.1404873 0.8595127 0.7245741 0.2754259 0.1457807 0.8542193 0.720762 0.2792379 0.1494383 0.8505617 0.7062029 0.2937971 0.7007457 0.2992544 0.1511068 0.8488932 0.1514734 0.8485266 0.06943351 0.8475321 0.08303433 0.08421045 0.8320215 0.08376801 0.08424568 0.8319494 0.0838049 0.06877446 0.8482013 0.08302414 0.6880994 0.3119006 0.6802631 0.3197369 0.149001 0.850999 0.1499855 0.8500145 0.08425837 0.8317759 0.08396577 0.06826001 0.8487381 0.08300197 0.6852878 0.3147122 0.6862474 0.3137526 0.1443221 0.8556779 0.1461514 0.8538486 0.08424752 0.8315316 0.08422094 0.06798291 0.8490456 0.08297145 0.6855325 0.3144676 0.6838363 0.3161637 0.1415138 0.8584862 0.1427472 0.8572527 0.08421295 0.8312467 0.08454036 0.06785041 0.8492141 0.08293545 0.6775666 0.3224334 0.6729141 0.3270859 0.1357168 0.8642832 0.1380145 0.8619856 0.08415281 0.8309425 0.08490467 0.06792819 0.8491708 0.082901 0.6536635 0.3463364 0.64212 0.3578799 0.1295675 0.8704325 0.1300477 0.8699523 0.08406943 0.8306815 0.085249 0.0685597 0.848562 0.08287823 0.6721305 0.3278694 0.6854298 0.3145703 0.1247478 0.8752521 0.1263696 0.8736304 0.08396774 0.8305024 0.08552986 0.06991624 0.8472142 0.08286958 0.7093945 0.2906054 0.7203024 0.2796976 0.1197009 0.8802991 0.1215709 0.8784291 0.08386111 0.8304461 0.0856927 0.07156223 0.8455696 0.08286809 0.7400851 0.2599148 0.7491689 0.2508311 0.1212259 0.8787741 0.1211881 0.8788119 0.08376103 0.8305136 0.0857253 0.07319957 0.8439293 0.08287107 0.7657268 0.2342731 0.7733949 0.2266051 0.1218346 0.8781654 0.1220949 0.8779051 0.0836749 0.8306736 0.08565145 0.07462966 0.8424909 0.08287942 0.7874205 0.2125795 0.7895553 0.2104447 0.1234803 0.8765196 0.1233847 0.8766154 0.08360904 0.8309153 0.08547562 0.07570117 0.8414018 0.08289706 0.7922544 0.2077456 0.7936283 0.2063716 0.1262049 0.8737952 0.1257309 0.874269 0.08356589 0.8312308 0.08520328 0.07641786 0.8406612 0.0829209 0.7943955 0.2056044 0.7938024 0.2061976 0.1338223 0.8661777 0.131669 0.868331 0.08354157 0.8315854 0.08487296 0.07686638 0.8401893 0.08294421 0.7928796 0.2071205 0.7921491 0.2078509 0.137907 0.862093 0.1369205 0.8630795 0.08352804 0.8319255 0.0845465 0.07714766 0.8398914 0.08296096 0.7901121 0.2098879 0.7890313 0.2109686 0.1433693 0.8566307 0.1418532 0.8581468 0.08351898 0.8322239 0.08425706 0.07736033 0.839673 0.08296662 0.7862178 0.2137823 0.7851144 0.2148855 0.1477894 0.8522105 0.1465829 0.8534172 0.08350694 0.8324416 0.08405143 0.07760983 0.8394308 0.08295935 0.7815671 0.2184329 0.7790503 0.2209497 0.1496447 0.8503553 0.1493173 0.8506827 0.08348721 0.832551 0.08396178 0.07802635 0.8390333 0.08294034 0.7928518 0.2071482 0.7996104 0.2003896 0.149142 0.850858 0.1495727 0.8504273 0.0834608 0.832547 0.08399224 0.07860797 0.8384781 0.08291393 0.8127221 0.1872779 0.8158616 0.1841384 0.1472201 0.8527799 0.1475409 0.852459 0.08343404 0.8324422 0.08412373 0.07910239 0.8380108 0.08288669 0.8221285 0.1778714 0.82346 0.17654 0.1434499 0.85655 0.14515 0.8548501 0.08341419 0.8322535 0.08433228 0.07935225 0.8377844 0.08286327 0.8262917 0.1737083 0.8259291 0.1740709 0.1371761 0.8628239 0.1398442 0.8601557 0.08340966 0.8320246 0.08456575 0.07929152 0.8378629 0.08284562 0.8254308 0.1745692 0.8238623 0.1761377 0.1323828 0.8676172 0.1346403 0.8653597 0.08342885 0.8318169 0.08475416 0.07889097 0.8382739 0.08283507 0.8209032 0.1790968 0.8177614 0.1822386 0.1342002 0.8657998 0.133954 0.866046 0.08347296 0.8316436 0.08488345 0.07813602 0.839031 0.08283293 0.8115105 0.1884894 0.806041 0.193959 0.1325944 0.8674056 0.1335276 0.8664723 0.08353686 0.8314772 0.08498591 0.0770291 0.8401307 0.08284014 0.7951214 0.2048786 0.7887431 0.211257 0.1273363 0.8726636 0.1300258 0.8699742 0.08361881 0.831353 0.08502811 0.0756219 0.8415212 0.08285689 0.7695046 0.2304953 0.7586519 0.2413482 0.1297796 0.8702204 0.1297486 0.8702515 0.08371484 0.8313111 0.08497405 0.07406085 0.8430576 0.08288156 0.7370873 0.2629127 0.7255532 0.2744469 0.1284986 0.8715015 0.1296939 0.870306 0.08381557 0.8313535 0.08483082 0.07263118 0.8444581 0.08291071 0.7003176 0.2996824 0.7064219 0.2935781 0.1340559 0.865944 0.1326169 0.8673831 0.0839141 0.8314862 0.08459973 0.0716806 0.8453741 0.08294522 0.716032 0.283968 0.7192242 0.2807759 0.1382889 0.861711 0.1358321 0.8641679 0.08400511 0.8316705 0.0843243 0.07110625 0.8459128 0.08298105 0.7233124 0.2766876 0.724137 0.275863 0.1441054 0.8558946 0.1422056 0.8577944 0.08408671 0.8318552 0.08405804 0.07061463 0.8463743 0.08301103 0.7236055 0.2763946 0.7223753 0.2776248 0.1483418 0.8516582 0.1470468 0.8529532 0.08415615 0.8319841 0.08385974 0.07006716 0.846903 0.08302986 0.7176911 0.2823089 0.7145169 0.2854831 0.1510574 0.8489426 0.1501744 0.8498255 0.06986075 0.8471099 0.08302921 0.06964886 0.8473204 0.0830307 0.08417218 0.8320156 0.08381205 0.08419042 0.8320286 0.083781 0.07043832 0.8465467 0.0830149 0.07025623 0.8467225 0.08302122 0.08410745 0.8319141 0.08397841 0.08413064 0.8319576 0.08391177 0.07093918 0.8460727 0.0829882 0.07077693 0.8462248 0.08299821 0.08402967 0.8317429 0.08422744 0.08405685 0.8318052 0.08413785 0.07146239 0.8455841 0.08295363 0.07127434 0.8457599 0.08296561 0.0839414 0.8315507 0.08450794 0.08397179 0.8316132 0.08441495 0.07224231 0.84484 0.08291774 0.07193171 0.8451389 0.08292937 0.08384531 0.831398 0.08475661 0.08387821 0.8314427 0.084679 0.07354992 0.843563 0.08288699 0.07306879 0.8440345 0.08289664 0.08374434 0.8313217 0.08493393 0.0837779 0.8313347 0.08488744 0.07510268 0.8420366 0.0828607 0.07457846 0.8425526 0.08286893 0.08364599 0.831339 0.08501505 0.08367812 0.8313248 0.08499711 0.0765863 0.8405729 0.0828408 0.07611614 0.8410375 0.08284634 0.08355832 0.8314351 0.08500659 0.08358556 0.8313919 0.0850225 0.07780182 0.8393678 0.08283036 0.07743161 0.8397355 0.08283281 0.0834884 0.8315947 0.08491694 0.08350968 0.8315387 0.08495151 0.07867592 0.8384945 0.08282953 0.07842385 0.8387473 0.08282881 0.08343726 0.8317654 0.08479738 0.08345204 0.831708 0.08483999 0.07919323 0.8379693 0.0828374 0.0790596 0.8381065 0.08283394 0.0834093 0.8319582 0.08463245 0.08341556 0.8318879 0.08469653 0.07936537 0.8377818 0.08285278 0.07934498 0.837808 0.08284693 0.08340662 0.8321861 0.08440721 0.08340501 0.8321084 0.08448654 0.07921385 0.8379116 0.08287447 0.07929748 0.8378359 0.08286672 0.08342248 0.8323951 0.08418244 0.08341568 0.8323315 0.08425277 0.0787906 0.8383086 0.0829007 0.07895648 0.8381519 0.08289164 0.08344769 0.8325299 0.08402234 0.08343869 0.8324951 0.08406615 0.07821023 0.8388614 0.08292835 0.07840752 0.8386729 0.08291941 0.08347505 0.8325696 0.08395528 0.08346617 0.8325681 0.08396571 0.07771885 0.8393308 0.08295029 0.07785511 0.839201 0.08294391 0.08349746 0.8324984 0.0840041 0.08349084 0.8325349 0.08397418 0.07743006 0.8396081 0.08296185 0.07751196 0.8395286 0.08295941 0.08351176 0.832315 0.0841732 0.08350783 0.8323879 0.08410423 0.07721751 0.839822 0.08296042 0.07728719 0.8397505 0.08296233 0.08352106 0.8320392 0.08443969 0.08351814 0.8321392 0.08434277 0.07697021 0.8400827 0.08294701 0.07706278 0.8399846 0.08295261 0.08353227 0.8317091 0.08475863 0.0835278 0.8318223 0.08464986 0.0765888 0.8404859 0.08292526 0.0767368 0.84033 0.08293312 0.0835523 0.8313561 0.08509153 0.08354431 0.831475 0.0849806 0.07597154 0.8411276 0.08290082 0.07620966 0.8408815 0.08290886 0.0835883 0.8310203 0.08539134 0.08357387 0.8311252 0.08530086 0.07502877 0.8420909 0.08288025 0.07538479 0.8417289 0.08288627 0.08364665 0.8307532 0.08560013 0.08362466 0.8308334 0.08554184 0.073704 0.8434267 0.08286929 0.07418262 0.8429453 0.08287203 0.08372658 0.830565 0.08570837 0.08369773 0.8306182 0.08568418 0.07211661 0.8450185 0.08286482 0.07266479 0.8444694 0.08286583 0.08382278 0.8304647 0.08571249 0.08378934 0.8304874 0.08572328 0.07044678 0.8466885 0.0828647 0.07099902 0.8461367 0.08286422 0.08392816 0.8304762 0.08559548 0.0838924 0.8304567 0.08565086 0.06895118 0.8481784 0.08287036 0.06940925 0.8477231 0.08286756 0.08403325 0.8306192 0.08534759 0.08399915 0.830559 0.08544182 0.06804412 0.8490681 0.08288782 0.06824535 0.8488746 0.08288007 0.08412349 0.8308546 0.08502185 0.08409571 0.8307681 0.08513617 0.06784087 0.8492396 0.08291941 0.06786358 0.8492286 0.08290779 0.08419185 0.8311529 0.08465522 0.08417183 0.8310506 0.08477753 0.06791967 0.8491243 0.08295601 0.06787472 0.8491814 0.08294391 0.08423489 0.8314484 0.08431661 0.08422338 0.8313533 0.0844233 0.06814485 0.8488664 0.08298879 0.06805384 0.8489676 0.0829786 0.08425343 0.8317077 0.08403891 0.08424973 0.8316255 0.08412474 0.06857109 0.8484148 0.08301401 0.06839549 0.8485979 0.08300656 0.08424866 0.8319095 0.0838418 0.08425295 0.8318513 0.08389574 0.06920737 0.8477638 0.08302873 0.06898552 0.8479892 0.08302527 0.08422064 0.8320175 0.08376187 0.08423244 0.8319932 0.08377432</float_array>
					  <technique_common>
						<accessor source="#Armature_Cylinder-skin-weights-array" count="960" stride="1">
						  <param name="WEIGHT" type="float"/>
						</accessor>
					  </technique_common>
					</source>
					<joints>
					  <input semantic="JOINT" source="#Armature_Cylinder-skin-joints"/>
					  <input semantic="INV_BIND_MATRIX" source="#Armature_Cylinder-skin-bind_poses"/>
					</joints>
					<vertex_weights count="384">
					  <input semantic="JOINT" source="#Armature_Cylinder-skin-joints" offset="0"/>
					  <input semantic="WEIGHT" source="#Armature_Cylinder-skin-weights" offset="1"/>
					  <vcount>2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 3 3 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 2 2 2 2 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 </vcount>
					  <v>
						  0 0    1 1

						  2 2    3 3

						  0 4    1 5
						  2 6    3 7

						  0 8    1 9

						  2 10   3 11

						  0 12   1 13

						  2 14 3 15 0 16 1 17 2 18 3 19 0 20 1 21 2 22 3 23 0 24 1 25 2 26 3 27 0 28 1 29 2 30 3 31 0 32 1 33 2 34 3 35 0 36 1 37 2 38 3 39 0 40 1 41 2 42 3 43 0 44 1 45 2 46 3 47 0 48 1 49 2 50 3 51 0 52 1 53 2 54 3 55 0 56 1 57 2 58 3 59 0 60 1 61 2 62 3 63 0 64 1 65 2 66 3 67 0 68 1 69 2 70 3 71 0 72 1 73 2 74 3 75 0 76 1 77 2 78 3 79 0 80 1 81 2 82 3 83 0 84 1 85 2 86 3 87 0 88 1 89 2 90 3 91 0 92 1 93 2 94 3 95 0 96 1 97 2 98 3 99 0 100 1 101 2 102 3 103 0 104 1 105 2 106 3 107 0 108 1 109 2 110 3 111 0 112 1 113 2 114 3 115 0 116 1 117 2 118 3 119 0 120 1 121 2 122 3 123 0 124 1 125 2 126 3 127 0 128 1 129 0 130 1 131 2 132 3 133 2 134 3 135 0 136 1 137 2 138 1 139 2 140 3 141 1 142 2 143 3 144 0 145 1 146 2 147 0 148 1 149 0 150 1 151 2 152 3 153 2 154 3 155 1 156 2 157 3 158 0 159 1 160 2 161 0 162 1 163 0 164 1 165 2 166 3 167 2 168 3 169 1 170 2 171 3 172 0 173 1 174 2 175 0 176 1 177 0 178 1 179 2 180 3 181 2 182 3 183 1 184 2 185 3 186 0 187 1 188 2 189 0 190 1 191 0 192 1 193 2 194 3 195 2 196 3 197 1 198 2 199 3 200 0 201 1 202 2 203 0 204 1 205 0 206 1 207 2 208 3 209 2 210 3 211 1 212 2 213 3 214 0 215 1 216 2 217 0 218 1 219 0 220 1 221 2 222 3 223 2 224 3 225 1 226 2 227 3 228 0 229 1 230 2 231 0 232 1 233 0 234 1 235 2 236 3 237 2 238 3 239 1 240 2 241 3 242 0 243 1 244 2 245 0 246 1 247 0 248 1 249 2 250 3 251 2 252 3 253 1 254 2 255 3 256 0 257 1 258 2 259 0 260 1 261 0 262 1 263 2 264 3 265 2 266 3 267 1 268 2 269 3 270 0 271 1 272 2 273 0 274 1 275 0 276 1 277 2 278 3 279 2 280 3 281 1 282 2 283 3 284 0 285 1 286 2 287 0 288 1 289 0 290 1 291 2 292 3 293 2 294 3 295 1 296 2 297 3 298 0 299 1 300 2 301 0 302 1 303 0 304 1 305 2 306 3 307 2 308 3 309 1 310 2 311 3 312 0 313 1 314 2 315 0 316 1 317 0 318 1 319 2 320 3 321 2 322 3 323 1 324 2 325 3 326 0 327 1 328 2 329 0 330 1 331 0 332 1 333 2 334 3 335 2 336 3 337 1 338 2 339 3 340 0 341 1 342 2 343 0 344 1 345 0 346 1 347 2 348 3 349 2 350 3 351 1 352 2 353 3 354 0 355 1 356 2 357 0 358 1 359 0 360 1 361 2 362 3 363 2 364 3 365 1 366 2 367 3 368 0 369 1 370 2 371 0 372 1 373 0 374 1 375 2 376 3 377 2 378 3 379 1 380 2 381 3 382 0 383 1 384 2 385 0 386 1 387 0 388 1 389 2 390 3 391 2 392 3 393 1 394 2 395 3 396 0 397 1 398 2 399 0 400 1 401 0 402 1 403 2 404 3 405 2 406 3 407 1 408 2 409 3 410 0 411 1 412 2 413 0 414 1 415 0 416 1 417 2 418 3 419 2 420 3 421 1 422 2 423 3 424 0 425 1 426 2 427 0 428 1 429 0 430 1 431 2 432 3 433 2 434 3 435 1 436 2 437 3 438 0 439 1 440 2 441 0 442 1 443 0 444 1 445 2 446 3 447 2 448 3 449 1 450 2 451 3 452 0 453 1 454 2 455 0 456 1 457 0 458 1 459 2 460 3 461 2 462 3 463 1 464 2 465 3 466 0 467 1 468 2 469 0 470 1 471 0 472 1 473 2 474 3 475 2 476 3 477 1 478 2 479 3 480 0 481 1 482 2 483 0 484 1 485 0 486 1 487 2 488 3 489 2 490 3 491 1 492 2 493 3 494 0 495 1 496 2 497 0 498 1 499 0 500 1 501 2 502 3 503 2 504 3 505 1 506 2 507 3 508 0 509 1 510 2 511 0 512 1 513 0 514 1 515 2 516 3 517 2 518 3 519 1 520 2 521 3 522 0 523 1 524 2 525 0 526 1 527 0 528 1 529 2 530 3 531 2 532 3 533 1 534 2 535 3 536 0 537 1 538 2 539 0 540 1 541 0 542 1 543 2 544 3 545 2 546 3 547 1 548 2 549 3 550 0 551 1 552 2 553 0 554 1 555 0 556 1 557 2 558 3 559 2 560 3 561 1 562 2 563 3 564 0 565 1 566 2 567 0 568 1 569 0 570 1 571 2 572 3 573 2 574 3 575 0 576 1 577 2 578 0 579 1 580 2 581 1 582 2 583 3 584 1 585 2 586 3 587 0 588 1 589 2 590 0 591 1 592 2 593 1 594 2 595 3 596 1 597 2 598 3 599 0 600 1 601 2 602 0 603 1 604 2 605 1 606 2 607 3 608 1 609 2 610 3 611 0 612 1 613 2 614 0 615 1 616 2 617 1 618 2 619 3 620 1 621 2 622 3 623 0 624 1 625 2 626 0 627 1 628 2 629 1 630 2 631 3 632 1 633 2 634 3 635 0 636 1 637 2 638 0 639 1 640 2 641 1 642 2 643 3 644 1 645 2 646 3 647 0 648 1 649 2 650 0 651 1 652 2 653 1 654 2 655 3 656 1 657 2 658 3 659 0 660 1 661 2 662 0 663 1 664 2 665 1 666 2 667 3 668 1 669 2 670 3 671 0 672 1 673 2 674 0 675 1 676 2 677 1 678 2 679 3 680 1 681 2 682 3 683 0 684 1 685 2 686 0 687 1 688 2 689 1 690 2 691 3 692 1 693 2 694 3 695 0 696 1 697 2 698 0 699 1 700 2 701 1 702 2 703 3 704 1 705 2 706 3 707 0 708 1 709 2 710 0 711 1 712 2 713 1 714 2 715 3 716 1 717 2 718 3 719 0 720 1 721 2 722 0 723 1 724 2 725 1 726 2 727 3 728 1 729 2 730 3 731 0 732 1 733 2 734 0 735 1 736 2 737 1 738 2 739 3 740 1 741 2 742 3 743 0 744 1 745 2 746 0 747 1 748 2 749 1 750 2 751 3 752 1 753 2 754 3 755 0 756 1 757 2 758 0 759 1 760 2 761 1 762 2 763 3 764 1 765 2 766 3 767 0 768 1 769 2 770 0 771 1 772 2 773 1 774 2 775 3 776 1 777 2 778 3 779 0 780 1 781 2 782 0 783 1 784 2 785 1 786 2 787 3 788 1 789 2 790 3 791 0 792 1 793 2 794 0 795 1 796 2 797 1 798 2 799 3 800 1 801 2 802 3 803 0 804 1 805 2 806 0 807 1 808 2 809 1 810 2 811 3 812 1 813 2 814 3 815 0 816 1 817 2 818 0 819 1 820 2 821 1 822 2 823 3 824 1 825 2 826 3 827 0 828 1 829 2 830 0 831 1 832 2 833 1 834 2 835 3 836 1 837 2 838 3 839 0 840 1 841 2 842 0 843 1 844 2 845 1 846 2 847 3 848 1 849 2 850 3 851 0 852 1 853 2 854 0 855 1 856 2 857 1 858 2 859 3 860 1 861 2 862 3 863 0 864 1 865 2 866 0 867 1 868 2 869 1 870 2 871 3 872 1 873 2 874 3 875 0 876 1 877 2 878 0 879 1 880 2 881 1 882 2 883 3 884 1 885 2 886 3 887 0 888 1 889 2 890 0 891 1 892 2 893 1 894 2 895 3 896 1 897 2 898 3 899 0 900 1 901 2 902 0 903 1 904 2 905 1 906 2 907 3 908 1 909 2 910 3 911 0 912 1 913 2 914 0 915 1 916 2 917 1 918 2 919 3 920 1 921 2 922 3 923 0 924 1 925 2 926 0 927 1 928 2 929 1 930 2 931 3 932 1 933 2 934 3 935 0 936 1 937 2 938 0 939 1 940 2 941 1 942 2 943 3 944 1 945 2 946 3 947 0 948 1 949 2 950 0 951 1 952 2 953 1 954 2 955 3 956 1 957 2 958 3 959</v>
					</vertex_weights>
				  </skin>
				</controller>
			  </library_controllers>
		</xml>""".trimIndent().toXml())
		println(skins)
	}

	@Test
	fun testLoadSkinedModel() = suspendTestNoJs {
		val library = resourcesVfs["skinning.dae"].readColladaLibrary()
		val instance = library.mainScene.instantiate()
		assertNotNull(instance["Cylinder"])
		assertEquals(true, instance["Cylinder"] is ViewWithMesh3D)
	}

	@Test
	fun testLoadTexturedModel() = suspendTestNoJs {
		val library = resourcesVfs["box_textured.dae"].readColladaLibrary()
		val instance = library.mainScene.instantiate()
		assertNotNull(instance["Cube"])
		assertEquals(true, instance["Cube"] is ViewWithMesh3D)
	}

    @Test
    fun testParseAnimations_animationsAsFlatStructure() = suspendTestNoJs {
        val library = resourcesVfs["animations_flat.dae"].readColladaLibrary()
        assertEquals(library.animationDefs.size, 1)
    }

    @Test
    fun testParseAnimations_animationsAsNestedStructure() = suspendTestNoJs {
        val library = resourcesVfs["animations_nested.dae"].readColladaLibrary()
        assertEquals(library.animationDefs.size, 1)
    }
}
