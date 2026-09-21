const fs = require('fs');
const path = require('path');

const ASSETS_DIR = path.resolve(__dirname, '../src/main/resources/assets/mekanism_complex_industries');
const BLOCKSTATES_DIR = path.join(ASSETS_DIR, 'blockstates');
const MODELS_BLOCK_DIR = path.join(ASSETS_DIR, 'models/block');
const MODELS_ITEM_DIR = path.join(ASSETS_DIR, 'models/item');

fs.mkdirSync(BLOCKSTATES_DIR, { recursive: true });
fs.mkdirSync(MODELS_BLOCK_DIR, { recursive: true });
fs.mkdirSync(MODELS_ITEM_DIR, { recursive: true });

function writeJson(filePath, data) {
    fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
    console.log(`Created: ${filePath}`);
}

// Blockstates
writeJson(path.join(BLOCKSTATES_DIR, 'solid_crude_oil_ore.json'), {
    variants: {
        "": { model: "mekanism_complex_industries:block/solid_crude_oil_ore" }
    }
});

writeJson(path.join(BLOCKSTATES_DIR, 'deepslate_solid_crude_oil_ore.json'), {
    variants: {
        "": { model: "mekanism_complex_industries:block/deepslate_solid_crude_oil_ore" }
    }
});

// Block models
writeJson(path.join(MODELS_BLOCK_DIR, 'solid_crude_oil_ore.json'), {
    parent: "minecraft:block/cube_all",
    textures: {
        all: "mekanism_complex_industries:block/solid_crude_oil_ore"
    }
});

writeJson(path.join(MODELS_BLOCK_DIR, 'deepslate_solid_crude_oil_ore.json'), {
    parent: "minecraft:block/cube_all",
    textures: {
        all: "mekanism_complex_industries:block/deepslate_solid_crude_oil_ore"
    }
});

// Item models
writeJson(path.join(MODELS_ITEM_DIR, 'solid_crude_oil_ore.json'), {
    parent: "mekanism_complex_industries:block/solid_crude_oil_ore"
});

writeJson(path.join(MODELS_ITEM_DIR, 'deepslate_solid_crude_oil_ore.json'), {
    parent: "mekanism_complex_industries:block/deepslate_solid_crude_oil_ore"
});

writeJson(path.join(MODELS_ITEM_DIR, 'petroleum_upgrade.json'), {
    parent: "minecraft:item/generated",
    textures: {
        layer0: "mekanism_complex_industries:item/petroleum_upgrade"
    }
});

writeJson(path.join(MODELS_ITEM_DIR, 'solid_crude_oil.json'), {
    parent: "minecraft:item/generated",
    textures: {
        layer0: "mekanism_complex_industries:item/solid_crude_oil"
    }
});

writeJson(path.join(MODELS_ITEM_DIR, 'crude_oil_bucket.json'), {
    parent: "minecraft:item/generated",
    textures: {
        layer0: "mekanism_complex_industries:item/crude_oil_bucket"
    }
});

console.log('All models and blockstates written successfully!');
