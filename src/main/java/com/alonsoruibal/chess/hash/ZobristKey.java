package com.alonsoruibal.chess.hash;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/* 
 * Computes the Polyglot key of a board
 * 
 */
public class ZobristKey {

    public static final long exclusionKey = 0x5472a27925a2a2f5L;

    public static final long[][] pawn = {{0x79ad695501e7d1e8L, 0x8249a47aee0e41f7L, 0x637a7780decfc0d9L, 0x19fc8a768cf4b6d4L, 0x7bcbc38da25a7f3cL, 0x5093417aa8a7ed5eL, 0x7fb9f855a997142L, 0x5355f900c2a82dc7L, 0xe99d662af4243939L, 0xa49cd132bfbf7cc4L, 0xce26c0b95c980d9L, 0xbb6e2924f03912eaL, 0x24c3c94df9c8d3f6L, 0xdabf2ac8201752fcL, 0xf145b6beccdea195L, 0x14acbaf4777d5776L, 0xf9b89d3e99a075c2L, 0x70ac4cd9f04f21f5L, 0x9a85ac909a24eaa1L, 0xee954d3c7b411f47L, 0x72b12c32127fed2bL, 0x54b3f4fa5f40d873L, 0x8535f040b9744ff1L, 0x27e6ad7891165c3fL, 0x8de8dca9f03cc54eL, 0xff07f64ef8ed14d0L, 0x92237ac237f3859L, 0x87bf02c6b49e2ae9L, 0x1920c04d47267bbdL, 0xae4a9346cc3f7cf2L, 0xa366e5b8c54f48b8L, 0x87b3e2b2b5c907b1L, 0x6304d09a0b3738c4L, 0x4f80f7a035dafb04L, 0x9a74acb964e78cb3L, 0x1e1032911fa78984L, 0x5bfea5b4712768e9L, 0x390e5fb44d01144bL, 0xb3f22c3d0b0b38edL, 0x9c1633264db49c89L, 0x7b32f7d1e03680ecL, 0xef927dbcf00c20f2L, 0xdfd395339cdbf4a7L, 0x6503080440750644L, 0x1881afc9a3a701d6L, 0x506aacf489889342L, 0x5b9b63eb9ceff80cL, 0x2171e64683023a08L, 0xede6c87f8477609dL, 0x3c79a0ff5580ef7fL, 0xf538639ce705b824L, 0xcf464cec899a2f8aL, 0x4a750a09ce9573f7L, 0xb5889c6e15630a75L, 0x5a7e8a57db91b77L, 0xb9fd7620e7316243L, 0x73a1921916591cbdL, 0x70eb093b15b290ccL, 0x920e449535dd359eL, 0x43fcae60cc0eba0L, 0xa246637cff328532L, 0x97d7374c60087b73L, 0x86536b8cf3428a8cL, 0x799e81f05bc93f31L}, {0xe83a908ff2fb60caL, 0xfbbad1f61042279L, 0x3290ac3a203001bfL, 0x75834465489c0c89L, 0x9c15f73e62a76ae2L, 0x44db015024623547L, 0x2af7398005aaa5c7L, 0x9d39247e33776d41L, 0x239f8b2d7ff719ccL, 0x5db4832046f3d9e5L, 0x11355146fd56395L, 0x40bdf15d4a672e32L, 0xd021ff5cd13a2ed5L, 0x9605d5f0e25ec3b0L, 0x1a083822ceafe02dL, 0xd7e765d58755c10L, 0x4bb38de5e7219443L, 0x331478f3af51bbe6L, 0xf3218f1c9510786cL, 0x82c7709e781eb7ccL, 0x7d11cdb1c3b7adf0L, 0x7449bbff801fed0bL, 0x679f848f6e8fc971L, 0x5d1a1ae85b49aa1L, 0x24aa6c514da27500L, 0xc9452ca81a09d85dL, 0x7b0500ac42047ac4L, 0xb4ab30f062b19abfL, 0x19f3c751d3e92ae1L, 0x87d2074b81d79217L, 0x8dbd98a352afd40bL, 0xaa649c6ebcfd50fcL, 0x735e2b97a4c45a23L, 0x3575668334a1dd3bL, 0x9d1bc9a3dd90a94L, 0x637b2b34ff93c040L, 0x3488b95b0f1850fL, 0xa71b9b83461cbd93L, 0x14a68fd73c910841L, 0x4c9f34427501b447L, 0xfcf7fe8a3430b241L, 0x5c82c505db9ab0faL, 0x51ebdc4ab9ba3035L, 0x9f74d14f7454a824L, 0xbf983fe0fe5d8244L, 0xd310a7c2ce9b6555L, 0x1fcbacd259bf02e7L, 0x18727070f1bd400bL, 0x96d693460cc37e5dL, 0x4de0b0f40f32a7b8L, 0x6568fca92c76a243L, 0x11d505d4c351bd7fL, 0x7ef48f2b83024e20L, 0xb9bc6c87167c33e7L, 0x8c74c368081b3075L, 0x3253a729b9ba3ddeL, 0xec16ca8aea98ad76L, 0x63dc359d8d231b78L, 0x93c5b5f47356388bL, 0x39f890f579f92f88L, 0x5f0f4a5898171bb6L, 0x42880b0236e4d951L, 0x6d2bdcdae2919661L, 0x42e240cb63689f2fL}};
    public static final long[][] rook = {{0xd18d8549d140caeaL, 0x1cfc8bed0d681639L, 0xca1e3785a9e724e5L, 0xb67c1fa481680af8L, 0xdfea21ea9e7557e3L, 0xd6b6d0ecc617c699L, 0xfa7e393983325753L, 0xa09e8c8c35ab96deL, 0x7983eed3740847d5L, 0x298af231c85bafabL, 0x2680b122baa28d97L, 0x734de8181f6ec39aL, 0x53898e4c3910da55L, 0x1761f93a44d5aefeL, 0xe4dbf0634473f5d2L, 0x4ed0fe7e9dc91335L, 0x261e4e4c0a333a9dL, 0x219b97e26ffc81bdL, 0x66b4835d9eafea22L, 0x4cc317fb9cddd023L, 0x50b704cab602c329L, 0xedb454e7badc0805L, 0x9e17e49642a3e4c1L, 0x66c1a2a1a60cd889L, 0x36f60e2ba4fa6800L, 0x38b6525c21a42b0eL, 0xf4f5d05c10cab243L, 0xcf3f4688801eb9aaL, 0x1ddc0325259b27deL, 0xb9571fa04dc089c8L, 0xd7504dfa8816edbbL, 0x1fe2cca76517db90L, 0xe699ed85b0dfb40dL, 0xd4347f66ec8941c3L, 0xf4d14597e660f855L, 0x8b889d624d44885dL, 0x258e5a80c7204c4bL, 0xaf0c317d32adaa8aL, 0x9c4cd6257c5a3603L, 0xeb3593803173e0ceL, 0xb090a7560a968e3L, 0x2cf9c8ca052f6e9fL, 0x116d0016cb948f09L, 0xa59e0bd101731a28L, 0x63767572ae3d6174L, 0xab4f6451cc1d45ecL, 0xc2a1e7b5b459aeb5L, 0x2472f6207c2d0484L, 0x804456af10f5fb53L, 0xd74bbe77e6116ac7L, 0x7c0828dd624ec390L, 0x14a195640116f336L, 0x2eab8ca63ce802d7L, 0xc6e57a78fbd986e0L, 0x58efc10b06a2068dL, 0xabeeddb2dde06ff1L, 0x12a8f216af9418c2L, 0xd4490ad526f14431L, 0xb49c3b3995091a36L, 0x5b45e522e4b1b4efL, 0xa1e9300cd8520548L, 0x49787fef17af9924L, 0x3219a39ee587a30L, 0xebe9ea2adf4321c7L}, {0x10dcd78e3851a492L, 0xb438c2b67f98e5e9L, 0x43954b3252dc25e5L, 0xab9090168dd05f34L, 0xce68341f79893389L, 0x36833336d068f707L, 0xdcdd7d20903d0c25L, 0xda3a361b1c5157b1L, 0xaf08da9177dda93dL, 0xac12fb171817eee7L, 0x1fff7ac80904bf45L, 0xa9119b60369ffebdL, 0xbfced1b0048eac50L, 0xb67b7896167b4c84L, 0x9b3cdb65f82ca382L, 0xdbc27ab5447822bfL, 0x6dd856d94d259236L, 0x67378d8eccef96cbL, 0x9fc477de4ed681daL, 0xf3b8b6675a6507ffL, 0xc3a9dc228caac9e9L, 0xc37b45b3f8d6f2baL, 0xb559eb1d04e5e932L, 0x1b0cab936e65c744L, 0x7440fb816508c4feL, 0x9d266d6a1cc0542cL, 0x4dda48153c94938aL, 0x74c04bf1790c0efeL, 0xe1925c71285279f5L, 0x8a8e849eb32781a5L, 0x73973751f12dd5eL, 0xa319ce15b0b4db31L, 0x94ebc8abcfb56daeL, 0xd7a023a73260b45cL, 0x72c8834a5957b511L, 0x8f8419a348f296bfL, 0x1e152328f3318deaL, 0x4838d65f6ef6748fL, 0xd6bf7baee43cac40L, 0x13328503df48229fL, 0xdd69a0d8ab3b546dL, 0x65ca5b96b7552210L, 0x2fd7e4b9e72cd38cL, 0x51d2b1ab2ddfb636L, 0x9d1d84fcce371425L, 0xa44cfe79ae538bbeL, 0xde68a2355b93cae6L, 0x9fc10d0f989993e0L, 0x3a938fee32d29981L, 0x2c5e9deb57ef4743L, 0x1e99b96e70a9be8bL, 0x764dbeae7fa4f3a6L, 0xaac40a2703d9bea0L, 0x1a8c1e992b941148L, 0x73aa8a564fb7ac9eL, 0x604d51b25fbf70e2L, 0x8fe88b57305e2ab6L, 0x89039d79d6fc5c5cL, 0x9bfb227ebdf4c5ceL, 0x7f7cc39420a3a545L, 0x3f6c6af859d80055L, 0xc8763c5b08d1908cL, 0x469356c504ec9f9dL, 0x26e6db8ffdf5adfeL}};
    public static final long[][] knight = {{0x3bba57b68871b59dL, 0xdf1d9f9d784ba010L, 0x94061b871e04df75L, 0x9315e5eb3a129aceL, 0x8bd35cc38336615L, 0xfe9a44e9362f05faL, 0x78e37644e7cad29eL, 0xc547f57e42a7444eL, 0x4f2a5cb07f6a35b3L, 0xa2f61bb6e437fdb5L, 0xa74049dac312ac71L, 0x336f52f8ff4728e7L, 0xd95be88cd210ffa7L, 0xd7f4f2448c0ceb81L, 0xf7a255d83bc373f8L, 0xd2b7adeeded1f73fL, 0x4c0563b89f495ac3L, 0x18fcf680573fa594L, 0xfcaf55c1bf8a4424L, 0x39b0bf7dde437ba2L, 0xf3a678cad9a2e38cL, 0x7ba2484c8a0fd54eL, 0x16b9f7e06c453a21L, 0x87d380bda5bf7859L, 0x35cab62109dd038aL, 0x32095b6d4ab5f9b1L, 0x3810e399b6f65ba2L, 0x9d1d60e5076f5b6fL, 0x7a1ee967d27579e2L, 0x68ca39053261169fL, 0x8cffa9412eb642c1L, 0x40e087931a00930dL, 0x9d1dfa2efc557f73L, 0x52ab92beb9613989L, 0x528f7c8602c5807bL, 0xd941aca44b20a45bL, 0x4361c0ca3f692f12L, 0x513e5e634c70e331L, 0x77a225a07cc2c6bdL, 0xa90b24499fcfafb1L, 0x284c847b9d887aaeL, 0x56fd23c8f9715a4cL, 0xcd9a497658a5698L, 0x5a110c6058b920a0L, 0x4208fe9e8f7f2d6L, 0x7a249a57ec0c9ba2L, 0x1d1260a51107fe97L, 0x722ff175f572c348L, 0x5e11e86d5873d484L, 0xed9b915c66ed37eL, 0xb0183db56ffc6a79L, 0x506e6744cd974924L, 0x881b82a13b51b9e2L, 0x9a9632e65904ad3cL, 0x742e1e651c60ba83L, 0x4feabfbbdb619cbL, 0x48cbff086ddf285aL, 0x99e7afeabe000731L, 0x93c42566aef98ffbL, 0xa865a54edcc0f019L, 0xd151d86adb73615L, 0xdab9fe6525d89021L, 0x1b85d488d0f20cc5L, 0xf678647e3519ac6eL}, {0xdd2c5bc84bc8d8fcL, 0xae623fd67468aa70L, 0xff6712ffcfd75ea1L, 0x930f80f4e8eb7462L, 0x45f20042f24f1768L, 0xbb215798d45df7afL, 0xefac4b70633b8f81L, 0x56436c9fe1a1aa8dL, 0xaa969b5c691ccb7aL, 0x43539603d6c55602L, 0x1bede3a3aef53302L, 0xdec468145b7605f6L, 0x808bd68e6ac10365L, 0xc91800e98fb99929L, 0x22fe545401165f1cL, 0x7eed120d54cf2dd9L, 0x28aed140be0bb7ddL, 0x10cff333e0ed804aL, 0x91b859e59ecb6350L, 0xb415938d7da94e3cL, 0x21f08570f420e565L, 0xded2d633cad004f6L, 0x65942c7b3c7e11aeL, 0xa87832d392efee56L, 0xaef3af4a563dfe43L, 0x480412bab7f5be2aL, 0xaf2042f5cc5c2858L, 0xef2f054308f6a2bcL, 0x9bc5a38ef729abd4L, 0x2d255069f0b7dab3L, 0x5648f680f11a2741L, 0xc5cc1d89724fa456L, 0x4dc4de189b671a1cL, 0x66f70b33fe09017L, 0x9da4243de836994fL, 0xbce5d2248682c115L, 0x11379625747d5af3L, 0xf4f076e65f2ce6f0L, 0x52593803dff1e840L, 0x19afe59ae451497fL, 0xf793c46702e086a0L, 0x763c4a1371b368fdL, 0x2df16f761598aa4fL, 0x21a007933a522a20L, 0xb3819a42abe61c87L, 0xb46ee9c5e64a6e7cL, 0xc07a3f80c31fb4b4L, 0x51039ab7712457c3L, 0x9ae182c8bc9474e8L, 0xb05ca3f564268d99L, 0xcfc447f1e53c8e1bL, 0x4850e73e03eb6064L, 0x2c604a7a177326b3L, 0xbf692b38d079f23L, 0xde336a2a4bc1c44bL, 0xd7288e012aeb8d31L, 0x6703df9d2924e97eL, 0x8ec97d2917456ed0L, 0x9c684cb6c4d24417L, 0xfc6a82d64b8655fbL, 0xf9b5b7c4acc67c96L, 0x69b97db1a4c03dfeL, 0xe755178d58fc4e76L, 0xa4fc4bd4fc5558caL}};
    public static final long[][] bishop = {{0x2fe4b17170e59750L, 0xe8d9ecbe2cf3d73fL, 0xb57d2e985e1419c7L, 0x572b974f03ce0bbL, 0xa8d7e4dab780a08dL, 0x4715ed43e8a45c0aL, 0xc330de426430f69dL, 0x23b70edb1955c4bfL, 0x49353fea39ba63b1L, 0xf85b2b4fbcde44b7L, 0xbe7444e39328a0acL, 0x3e2b8bcbf016d66dL, 0x964e915cd5e2b207L, 0x1725cabfcb045b00L, 0x7fbf21ec8a1f45ecL, 0x11317ba87905e790L, 0xe94c39a54a98307fL, 0xaa70b5b4f89695a2L, 0x3bdbb92c43b17f26L, 0xcccb7005c6b9c28dL, 0x18a6a990c8b35ebdL, 0xfc7c95d827357afaL, 0x1fca8a92fd719f85L, 0x1dd01aafcd53486aL, 0xdbc0d2b6ab90a559L, 0x94628d38d0c20584L, 0x64972d68dee33360L, 0xb9c11d5b1e43a07eL, 0x2de0966daf2f8b1cL, 0x2e18bc1ad9704a68L, 0xd4dba84729af48adL, 0xb7a0b174cff6f36eL, 0xcffe1939438e9b24L, 0x79999cdff70902cbL, 0x8547eddfb81ccb94L, 0x7b77497b32503b12L, 0x97fcaacbf030bc24L, 0x6ced1983376fa72bL, 0x7e75d99d94a70f4dL, 0xd2733c4335c6a72fL, 0x9ff38fed72e9052fL, 0x9f65789a6509a440L, 0x981dcd296a8736dL, 0x5873888850659ae7L, 0xc678b6d860284a1cL, 0x63e22c147b9c3403L, 0x92fae24291f2b3f1L, 0x829626e3892d95d7L, 0x7a76956c3eafb413L, 0x7f5126dbba5e0ca7L, 0x12153635b2c0cf57L, 0x7b3f0195fc6f290fL, 0x5544f7d774b14aefL, 0x56c074a581ea17feL, 0xe7f28ecd2d49eecdL, 0xe479ee5b9930578cL, 0x7f9d1a2e1ebe1327L, 0x5d0a12f27ad310d1L, 0x3bc36e078f7515d7L, 0x4da8979a0041e8a9L, 0x950113646d1d6e03L, 0x7b4a38e32537df62L, 0x8a1b083821f40cb4L, 0x3d5774a11d31ab39L}, {0x501f65edb3034d07L, 0x907f30421d78c5deL, 0x1a804aadb9cfa741L, 0xce2a38c344a6eedL, 0xd363eff5f0977996L, 0x2cd16e2abd791e33L, 0x58627e1a149bba21L, 0x7f9b6af1ebf78bafL, 0x364f6ffa464ee52eL, 0x6c3b8e3e336139d3L, 0xf943aee7febf21b8L, 0x88e049589c432e0L, 0xd49503536abca345L, 0x3a6c27934e31188aL, 0x957baf61700cff4eL, 0x37624ae5a48fa6e9L, 0xb344c470397bba52L, 0xbac7a9a18531294bL, 0xecb53939887e8175L, 0x565601c0364e3228L, 0xef1955914b609f93L, 0x16f50edf91e513afL, 0x56963b0dca418fc0L, 0xd60f6dcedc314222L, 0x99170a5dc3115544L, 0x59b97885e2f2ea28L, 0xbc4097b116c524d2L, 0x7a13f18bbedc4ff5L, 0x71582401c38434dL, 0xb422061193d6f6a7L, 0xb4b81b3fa97511e2L, 0x65d34954daf3cebdL, 0xc7d9f16864a76e94L, 0x7bd94e1d8e17debcL, 0xd873db391292ed4fL, 0x30f5611484119414L, 0x565c31f7de89ea27L, 0xd0e4366228b03343L, 0x325928ee6e6f8794L, 0x6f423357e7c6a9f9L, 0x35dd37d5871448afL, 0xb03031a8b4516e84L, 0xb3f256d8aca0b0b9L, 0xfd22063edc29fcaL, 0xd9a11fbb3d9808e4L, 0x3a9bf55ba91f81caL, 0xc8c93882f9475f5fL, 0x947ae053ee56e63cL, 0xbbe83f4ecc2bdecbL, 0xcd454f8f19c5126aL, 0xc62c58f97dd949bfL, 0x693501d628297551L, 0xb9ab4ce57f2d34f3L, 0x9255abb50d532280L, 0xebfafa33d7254b59L, 0xe9f6082b05542e4eL, 0x98954d51fff6580L, 0x8107fccf064fcf56L, 0x852f54934da55cc9L, 0x9c7e552bc76492fL, 0xe9f6760e32cd8021L, 0xa3bc941d0a5061cbL, 0xba89142e007503b8L, 0xdc842b7e2819e230L}};
    public static final long[][] queen = {{0x720bf5f26f4d2eaaL, 0x1c2559e30f0946beL, 0xe328e230e3e2b3fbL, 0x87e79e5a57d1d13L, 0x8dd9bdfd96b9f63L, 0x64d0e29eea8838b3L, 0xddf957bc36d8b9caL, 0x6ffe73e81b637fb3L, 0x93b633abfa3469f8L, 0xe846963877671a17L, 0x59ac2c7873f910a3L, 0x660d3257380841eeL, 0xd813f2fab7f5c5caL, 0x4112cf68649a260eL, 0x443f64ec5a371195L, 0xb0774d261cc609dbL, 0xb5635c95ff7296e2L, 0xed2df21216235097L, 0x4a29c6465a314cd1L, 0xd83cc2687a19255fL, 0x506c11b9d90e8b1dL, 0x57277707199b8175L, 0xcaf21ecd4377b28cL, 0xc0c0f5a60ef4cdcfL, 0x7c45d833aff07862L, 0xa5b1cfdba0ab4067L, 0x6ad047c430a12104L, 0x6c47bec883a7de39L, 0x944f6de09134dfb6L, 0x9aeba33ac6ecc6b0L, 0x52e762596bf68235L, 0x22af003ab672e811L, 0x50065e535a213cf6L, 0xde0c89a556b9ae70L, 0xd1e0ccd25bb9c169L, 0x6b17b224bad6bf27L, 0x6b02e63195ad0cf8L, 0x455a4b4cfe30e3f5L, 0x9338e69c052b8e7bL, 0x5092ef950a16da0bL, 0x67fef95d92607890L, 0x31865ced6120f37dL, 0x3a6853c7e70757a7L, 0x32ab0edb696703d3L, 0xee97f453f06791edL, 0x6dc93d9526a50e68L, 0x78edefd694af1eedL, 0x9c1169fa2777b874L, 0x6bfa9aae5ec05779L, 0x371f77e76bb8417eL, 0x3550c2321fd6109cL, 0xfb4a3d794a9a80d2L, 0xf43c732873f24c13L, 0xaa9119ff184cccf4L, 0xb69e38a8965c6b65L, 0x1f2b1d1f15f6dc9cL, 0xb5b4071dbfc73a66L, 0x8f9887e6078735a1L, 0x8de8a1c7797da9bL, 0xfcb6be43a9f2fe9bL, 0x49a7f41061a9e60L, 0x9f91508bffcfc14aL, 0xe3273522064480caL, 0xcd04f3ff001a4778L}, {0x1bda0492e7e4586eL, 0xd23c8e176d113600L, 0x252f59cf0d9f04bbL, 0xb3598080ce64a656L, 0x993e1de72d36d310L, 0xa2853b80f17f58eeL, 0x1877b51e57a764d5L, 0x1f837cc7350524L, 0x241260ed4ad1e87dL, 0x64c8e531bff53b55L, 0xca672b91e9e4fa16L, 0x3871700761b3f743L, 0xf95cffa23af5f6f4L, 0x8d14dedb30be846eL, 0x3b097adaf088f94eL, 0x21e0bd5026c619bfL, 0xb8d91274b9e9d4fbL, 0x1db956e450275779L, 0x4fc8e9560f91b123L, 0x63573ff03e224774L, 0x647dfedcd894a29L, 0x7884d9bc6cb569d8L, 0x7fba195410e5ca30L, 0x106c09b972d2e822L, 0x98f076a4f7a2322eL, 0x70cb6af7c2d5bcf0L, 0xb64be8d8b25396c1L, 0xa9aa4d20db084e9bL, 0x2e6d02c36017f67fL, 0xefed53d75fd64e6bL, 0xd9f1f30ccd97fb09L, 0xa2ebee47e2fbfce1L, 0xfc87614baf287e07L, 0x240ab57a8b888b20L, 0xbf8d5108e27e0d48L, 0x61bdd1307c66e300L, 0xb925a6cd0421aff3L, 0x3e003e616a6591e9L, 0x94c3251f06f90cf3L, 0xbf84470805e69b5fL, 0x758f450c88572e0bL, 0x1b6baca2ae4e125bL, 0x61cf4f94c97df93dL, 0x2738259634305c14L, 0xd39bb9c3a48db6cfL, 0x8215e577001332c8L, 0xa1082c0466df6c0aL, 0xef02cdd06ffdb432L, 0x7976033a39f7d952L, 0x106f72fe81e2c590L, 0x8c90fd9b083f4558L, 0xfd080d236da814baL, 0x7b64978555326f9fL, 0x60e8ed72c0dff5d1L, 0xb063e962e045f54dL, 0x959f587d507a8359L, 0x1a4e4822eb4d7a59L, 0x5d94337fbfaf7f5bL, 0xd30c088ba61ea5efL, 0x9d765e419fb69f6dL, 0x9e21f4f903b33fd9L, 0xb4d8f77bc3e56167L, 0x733ea705fae4fa77L, 0xa4ec0132764ca04bL}};
    public static final long[][] king = {{0x2102ae466ebb1148L, 0xe87fbb46217a360eL, 0x310cb380db6f7503L, 0xb5fdfc5d3132c498L, 0xdaf8e9829fe96b5fL, 0xcac09afbddd2cdb4L, 0xb862225b055b6960L, 0x55b6344cf97aafaeL, 0x46e3ecaaf453ce9L, 0x962aceefa82e1c84L, 0xf5b4b0b0d2deeeb4L, 0x1af3dbe25d8f45daL, 0xf9f4892ed96bd438L, 0xc4c118bfe78feaaeL, 0x7a69afdcc42261aL, 0xf8549e1a3aa5e00dL, 0x486289ddcc3d6780L, 0x222bbfae61725606L, 0x2bc60a63a6f3b3f2L, 0x177e00f9fc32f791L, 0x522e23f3925e319eL, 0x9c2ed44081ce5fbdL, 0x964781ce734b3c84L, 0xf05d129681949a4cL, 0xd586bd01c5c217f6L, 0x233003b5a6cfe6adL, 0x24c0e332b70019b0L, 0x9da058c67844f20cL, 0xe4d9429322cd065aL, 0x1fab64ea29a2ddf7L, 0x8af38731c02ba980L, 0x7dc7785b8efdfc80L, 0x93cbe0b699c2585dL, 0x1d95b0a5fcf90bc6L, 0x17efee45b0dee640L, 0x9e4c1269baa4bf37L, 0xd79476a84ee20d06L, 0xa56a5f0bfe39272L, 0x7eba726d8c94094bL, 0x5e5637885f29bc2bL, 0xc61bb3a141e50e8cL, 0x2785338347f2ba08L, 0x7ca9723fbb2e8988L, 0xce2f8642ca0712dcL, 0x59300222b4561e00L, 0xc2b5a03f71471a6fL, 0xd5f9e858292504d5L, 0x65fa4f227a2b6d79L, 0x71f1ce2490d20b07L, 0xe6c42178c4bbb92eL, 0xa9c32d5eae45305L, 0xc335248857fa9e7L, 0x142de49fff7a7c3dL, 0x64a53dc924fe7ac9L, 0x9f6a419d382595f4L, 0x150f361dab9dec26L, 0xd20d8c88c8ffe65fL, 0x917f1dd5f8886c61L, 0x56986e2ef3ed091bL, 0x5fa7867caf35e149L, 0x81a1549fd6573da5L, 0x96fbf83a12884624L, 0xe728e8c83c334074L, 0xf1bcc3d275afe51aL}, {0xd6b04d3b7651dd7eL, 0xe34a1d250e7a8d6bL, 0x53c065c6c8e63528L, 0x1bdea12e35f6a8c9L, 0x21874b8b4d2dbc4fL, 0x3a88a0fbbcb05c63L, 0x43ed7f5a0fae657dL, 0x230e343dfba08d33L, 0xd4c718bc4ae8ae5fL, 0x9eedeca8e272b933L, 0x10e8b35af3eeab37L, 0xe09b88e1914f7afL, 0x3fa9ddfb67e2f199L, 0xb10bb459132d0a26L, 0x2c046f22062dc67dL, 0x5e90277e7cb39e2dL, 0xb49b52e587a1ee60L, 0xac042e70f8b383f2L, 0x89c350c893ae7dc1L, 0xb592bf39b0364963L, 0x190e714fada5156eL, 0xec8177f83f900978L, 0x91b534f885818a06L, 0x81536d601170fc20L, 0x57e3306d881edb4fL, 0xa804d18b7097475L, 0xe74733427b72f0c1L, 0x24b33c9d7ed25117L, 0xe805a1e290cf2456L, 0x3b544ebe544c19f9L, 0x3e666e6f69ae2c15L, 0xfb152fe3ff26da89L, 0x1a4ff12616eefc89L, 0x990a98fd5071d263L, 0x84547ddc3e203c94L, 0x7a3aec79624c7daL, 0x8a328a1cedfe552cL, 0xd1e649de1e7f268bL, 0x2d8d5432157064c8L, 0x4ae7d6a36eb5dbcbL, 0x4659d2b743848a2cL, 0x19ebb029435dcb0fL, 0x4e9d2827355fc492L, 0xccec0a73b49c9921L, 0x46c9feb55d120902L, 0x8d2636b81555a786L, 0x30c05b1ba332f41cL, 0xf6f7fd1431714200L, 0xabbdcdd7ed5c0860L, 0x9853eab63b5e0b35L, 0x352787baa0d7c22fL, 0xc7f6aa2de59aea61L, 0x3727073c2e134b1L, 0x5a0f544dd2b1fb18L, 0x74f85198b05a2e7dL, 0x963ef2c96b33be31L, 0xff577222c14f0a3aL, 0x4e4b705b92903ba4L, 0x730499af921549ffL, 0x13ae978d09fe5557L, 0xd9e92aa246bf719eL, 0x7a4c10ec2158c4a6L, 0x49cad48cebf4a71eL, 0xcf05daf5ac8d77b0L}};
    public static final long whiteKingSideCastling = 0x31d71dce64b2c310L;
    public static final long whiteQueenSideCastling = 0xf165b587df898190L;
    public static final long blackKingSideCastling = 0xa57e6339dd2cf3a0L;
    public static final long blackQueenSideCastling = 0x1ef6e6dbb1961ec9L;
    public static final long[] passantColumn = {0x70cc73d90bc26e24L, 0xe21a6b35df0c3ad7L, 0x3a93d8b2806962L, 0x1c99ded33cb890a1L, 0xcf3145de0add4289L, 0xd0e4427a5514fb72L, 0x77c621cc9fb3a483L, 0x67a34dac4356550bL,};
    public static final long whiteMove = 0xf8d626aaaf278509L;

    /**
     *
     */
    public static long getKeyPieceIndex(int index, char pieceChar) {
        switch (pieceChar) {
            case 'P':
                return pawn[0][index];
            case 'p':
                return pawn[1][index];
            case 'R':
                return rook[0][index];
            case 'r':
                return rook[1][index];
            case 'N':
                return knight[0][index];
            case 'n':
                return knight[1][index];
            case 'B':
                return bishop[0][index];
            case 'b':
                return bishop[1][index];
            case 'Q':
                return queen[0][index];
            case 'q':
                return queen[1][index];
            case 'K':
                return king[0][index];
            case 'k':
                return king[1][index];
        }
        return 0;
    }

    public static long[] getKey(Board board) {
        long key[] = {0, 0};

        long square = BitboardUtils.H1;
        byte index = 0;
        int color;
        while (square != 0) {
            color = (square & board.whites) != 0 ? 0 : 1;
            key[color] ^= getKeyPieceIndex(index, board.getPieceAt(square));
            square <<= 1;
            index++;
        }

        if (board.getWhiteKingsideCastling()) {
            key[0] ^= whiteKingSideCastling;
        }
        if (board.getWhiteQueensideCastling()) {
            key[0] ^= whiteQueenSideCastling;
        }
        if (board.getBlackKingsideCastling()) {
            key[1] ^= blackKingSideCastling;
        }
        if (board.getBlackQueensideCastling()) {
            key[1] ^= blackQueenSideCastling;
        }
        // passant flags only when pawn can capture
        long passant = board.getPassantSquare();
        if ((passant != 0)
                && (((!board.getTurn() && (((passant << 9) | (passant << 7)) & board.blacks & board.pawns) != 0))
                || ((board.getTurn() && (((passant >>> 9) | (passant >>> 7)) & board.whites & board.pawns) != 0)))) {
            color = board.getTurn() ? 0 : 1; // TODO test
            key[1 - color] ^= passantColumn[BitboardUtils.getColumn(passant)];
        }
        if (board.getTurn()) {
            key[0] ^= whiteMove;
        }
        return key;
    }
}
