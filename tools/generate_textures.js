const fs = require('fs');
const path = require('path');
const sharp = require('e:/Program Files/McpTools/mcmodding-install/node_modules/sharp');

const ASSETS_DIR = path.resolve(__dirname, '../src/main/resources/assets/mekanism_complex_industries/textures');
const ITEM_DIR = path.join(ASSETS_DIR, 'item');
const BLOCK_DIR = path.join(ASSETS_DIR, 'block');

fs.mkdirSync(ITEM_DIR, { recursive: true });
fs.mkdirSync(BLOCK_DIR, { recursive: true });

function parseHex(hex) {
    hex = hex.replace('#', '');
    if (hex.length === 6) {
        return [
            parseInt(hex.slice(0, 2), 16),
            parseInt(hex.slice(2, 4), 16),
            parseInt(hex.slice(4, 6), 16),
            255
        ];
    } else if (hex.length === 8) {
        return [
            parseInt(hex.slice(0, 2), 16),
            parseInt(hex.slice(2, 4), 16),
            parseInt(hex.slice(4, 6), 16),
            parseInt(hex.slice(6, 8), 16)
        ];
    }
    return [0, 0, 0, 0];
}

async function writePng(filename, width, height, pixelMap, palette) {
    const buffer = Buffer.alloc(width * height * 4);
    for (let y = 0; y < height; y++) {
        for (let x = 0; x < width; x++) {
            const char = pixelMap[y][x];
            const color = palette[char] || [0, 0, 0, 0];
            const idx = (y * width + x) * 4;
            buffer[idx] = color[0];
            buffer[idx + 1] = color[1];
            buffer[idx + 2] = color[2];
            buffer[idx + 3] = color[3];
        }
    }
    await sharp(buffer, { raw: { width, height, channels: 4 } }).png().toFile(filename);
    console.log(`Generated: ${filename}`);
}

async function generatePetroleumUpgrade() {
    // 16x16 Mekanism-style Upgrade Card with Oil Droplet Core & Gold Connector Pins
    const palette = {
        ' ': [0, 0, 0, 0],
        'H': parseHex('94A3B8'), // Light steel highlight
        'B': parseHex('64748B'), // Steel border
        'M': parseHex('475569'), // Card metallic body
        'D': parseHex('334155'), // Dark metal
        'W': parseHex('1E293B'), // Recessed window frame
        'K': parseHex('0F172A'), // Window background
        'o': parseHex('1C1917'), // Pitch black oil
        'O': parseHex('292524'), // Dark oil body
        'L': parseHex('44403C'), // Oil reflection
        'S': parseHex('38BDF8'), // Azure energy glow / specular
        's': parseHex('0284C7'), // Azure edge
        'G': parseHex('F59E0B'), // Gold pin
        'g': parseHex('B45309'), // Gold pin shadow
        'C': parseHex('10B981'), // Circuit emerald indicator
    };

    const map = [
        "  BBBBBBBBBBBB  ",
        " BHHHHHHHHHHHHB ",
        " BMMMMMMMMMMMMB ",
        " BMMWWWWWWWWMMB ",
        " BMDWKKssKKWDMB ",
        " BMDWKSSsoKWDMB ",
        " BMDWKoSOOoWDMB ",
        " BMDWKOOoooWDMB ",
        " BMDWKKoooKWDMB ",
        " BMMWWWWWWWWMMB ",
        " BMCMMMMMMMMMMB ",
        " BMMMMMMMMMMMMB ",
        " BDDDDDDDDDDDDB ",
        "  B G g  G g B  ",
        "    G g  G g    ",
        "                "
    ];

    await writePng(path.join(ITEM_DIR, 'petroleum_upgrade.png'), 16, 16, map, palette);
}

async function generateSolidCrudeOil() {
    // 16x16 Mineral Chunk of Solid Bitumen / High-Density Crude Oil
    const palette = {
        ' ': [0, 0, 0, 0],
        'o': parseHex('0C0A09'), // Deepest black
        'O': parseHex('1C1917'), // Pitch black bitumen
        'D': parseHex('292524'), // Dark body
        'M': parseHex('3D3835'), // Midtone asphalt
        'L': parseHex('57534E'), // Surface reflection
        'H': parseHex('8D857E'), // High specular
        'W': parseHex('D6D3D1'), // White flash sparkle
        'B': parseHex('050404'), // Outline
    };

    const map = [
        "                ",
        "     BBBBB      ",
        "    BWWHLOB     ",
        "   BWHLLMDDB    ",
        "  BHLMDDDOOOB   ",
        " BHLMDDDDOOOoB  ",
        " BMMDDDOOOOOooB ",
        " BDDDOOOOoooOoB ",
        " BDOOOOoooooooB ",
        " BDOOOooWLoOooB ",
        "  BOOoooHDOoooB ",
        "  BOOooooOoooB  ",
        "   BooOoooooB   ",
        "    BBooooBB    ",
        "      BBBB      ",
        "                "
    ];

    await writePng(path.join(ITEM_DIR, 'solid_crude_oil.png'), 16, 16, map, palette);
}

async function generateCrudeOilBucket() {
    // 16x16 Minecraft Iron Bucket filled with Crude Oil
    const palette = {
        ' ': [0, 0, 0, 0],
        'b': parseHex('33363B'), // Bucket dark outline
        'B': parseHex('545963'), // Bucket medium iron
        'I': parseHex('8D94A0'), // Bucket bright iron
        'H': parseHex('C2CAD4'), // Bucket highlight
        'o': parseHex('0C0A09'), // Deep oil
        'O': parseHex('1C1917'), // Oil body
        'M': parseHex('2D2825'), // Oil sheen
        'S': parseHex('4A433D'), // Oil surface reflection
    };

    const map = [
        "      b b       ",
        "     bHI b      ",
        "    bHIbb b     ",
        "    bIb   b     ",
        "   bHIb b  b    ",
        "   bIb   b b    ",
        "  bHIbSSMMb b   ",
        "  bIbSMOOOObb   ",
        " bHIbMOOOOOob   ",
        " bIbOOOOOoobb   ",
        " bIbOOOOooobb   ",
        " bIbOOOooobbb   ",
        "  bIbOooobbb    ",
        "  bIbOoobbb     ",
        "   bIbIbbb      ",
        "    bbbb        "
    ];

    await writePng(path.join(ITEM_DIR, 'crude_oil_bucket.png'), 16, 16, map, palette);
}

async function generateSolidCrudeOilOre() {
    // 16x16 Stone Ore with glossy black oil crystal veins
    const palette = {
        '0': parseHex('595959'), // Stone dark
        '1': parseHex('686868'), // Stone base 1
        '2': parseHex('747474'), // Stone base 2
        '3': parseHex('808080'), // Stone light 1
        '4': parseHex('8C8C8C'), // Stone light 2
        'o': parseHex('0C0A09'), // Oil deepest
        'O': parseHex('1A1715'), // Oil black
        'D': parseHex('292523'), // Oil dark body
        'L': parseHex('443F3B'), // Oil sheen
        'H': parseHex('78716C'), // Specular highlight
        'W': parseHex('E7E5E4'), // Bright glint
    };

    const map = [
        "2123321221343212",
        "1234321012332112",
        "234321oO21223212",
        "34321oWHD2134321",
        "2321oOHLOD123321",
        "1211ODDDOOo12110",
        "21221ODoOo122122",
        "323421oo12343212",
        "4344321234321223",
        "323321oOD1212334",
        "21211oWHLD123443",
        "1221oOHLODO12332",
        "23211ODOOo121221",
        "343211oOo1223321",
        "2332121123432112",
        "1212232123321221"
    ];

    await writePng(path.join(BLOCK_DIR, 'solid_crude_oil_ore.png'), 16, 16, map, palette);
}

async function generateDeepslateSolidCrudeOilOre() {
    // 16x16 Deepslate Ore with glossy black oil crystal veins
    const palette = {
        '0': parseHex('242528'), // Deepslate darkest
        '1': parseHex('313236'), // Deepslate dark
        '2': parseHex('3F4045'), // Deepslate mid
        '3': parseHex('4D4E54'), // Deepslate light
        '4': parseHex('5B5D64'), // Deepslate highlight
        'o': parseHex('080706'), // Oil deepest
        'O': parseHex('171513'), // Oil black
        'D': parseHex('262220'), // Oil dark body
        'L': parseHex('403A36'), // Oil sheen
        'H': parseHex('6E6660'), // Specular highlight
        'W': parseHex('D6D3D1'), // Bright glint
    };

    const map = [
        "1122332211001122",
        "2233443322112233",
        "1122oOD211001122",
        "001oWHLD00001122",
        "112oOHLODO112233",
        "2231ODDDOOo23344",
        "33421ODoOo133443",
        "223311oo11223322",
        "1122110011223322",
        "001121oOD1100112",
        "11221oWHLD112233",
        "2233oOHLODO23344",
        "33421ODOOo133443",
        "223311oOo1122332",
        "1122110011221100",
        "0011221100112211"
    ];

    await writePng(path.join(BLOCK_DIR, 'deepslate_solid_crude_oil_ore.png'), 16, 16, map, palette);
}

async function generateFluidTextures() {
    // 16x16 Crude Oil Still & Flowing textures
    const palette = {
        '1': parseHex('12100E'),
        '2': parseHex('1A1715'),
        '3': parseHex('24201D'),
        '4': parseHex('2E2925'),
        '5': parseHex('3B3530'),
    };

    const stillMap = [
        "2233221122332211",
        "2344322234432212",
        "3454323345432223",
        "3443223344322233",
        "2332212233221122",
        "2211223322112233",
        "2222344322122344",
        "2334543222334543",
        "3344322233344322",
        "2332211223322112",
        "2211223322112233",
        "1222344322122344",
        "2233454322233454",
        "3344322233344322",
        "2332211223322112",
        "2211223322112233"
    ];

    await writePng(path.join(BLOCK_DIR, 'crude_oil_still.png'), 16, 16, stillMap, palette);
    await writePng(path.join(BLOCK_DIR, 'crude_oil_flow.png'), 16, 16, stillMap, palette);

    // Write mcmeta for flow animation
    const mcmeta = {
        animation: {
            frametime: 4,
            interpolate: true
        }
    };
    fs.writeFileSync(path.join(BLOCK_DIR, 'crude_oil_flow.png.mcmeta'), JSON.stringify(mcmeta, null, 2));
    console.log('Generated: crude_oil_flow.png.mcmeta');
}

async function main() {
    await generatePetroleumUpgrade();
    await generateSolidCrudeOil();
    await generateCrudeOilBucket();
    await generateSolidCrudeOilOre();
    await generateDeepslateSolidCrudeOilOre();
    await generateFluidTextures();
    console.log('All textures generated successfully!');
}

main().catch(err => {
    console.error(err);
    process.exit(1);
});
