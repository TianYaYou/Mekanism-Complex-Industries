const fs = require('fs');
const path = require('path');

const DATA_DIR = path.resolve(__dirname, '../src/main/resources/data');

function writeJson(relPath, obj) {
    const fullPath = path.join(DATA_DIR, relPath);
    fs.mkdirSync(path.dirname(fullPath), { recursive: true });
    fs.writeFileSync(fullPath, JSON.stringify(obj, null, 2));
    console.log(`Generated: ${relPath}`);
}

// 1. Crafting Recipe: Petroleum Upgrade
writeJson('mekanism_complex_industries/recipes/petroleum_upgrade.json', {
    type: "minecraft:crafting_shaped",
    pattern: [
        " G ",
        "WNL",
        " G "
    ],
    key: {
        "G": { "tag": "forge:glass" },
        "W": { "item": "minecraft:water_bucket" },
        "N": { "item": "minecraft:netherite_ingot" },
        "L": { "item": "minecraft:lava_bucket" }
    },
    result: {
        item: "mekanism_complex_industries:petroleum_upgrade",
        count: 1
    }
});

// 2. Mekanism Enriching Recipes: 8x yield!
writeJson('mekanism_complex_industries/recipes/enriching/solid_crude_oil_from_ore.json', {
    type: "mekanism:enriching",
    input: {
        ingredient: {
            item: "mekanism_complex_industries:solid_crude_oil_ore"
        }
    },
    output: {
        item: "mekanism_complex_industries:solid_crude_oil",
        count: 8
    }
});

writeJson('mekanism_complex_industries/recipes/enriching/solid_crude_oil_from_deepslate_ore.json', {
    type: "mekanism:enriching",
    input: {
        ingredient: {
            item: "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
        }
    },
    output: {
        item: "mekanism_complex_industries:solid_crude_oil",
        count: 8
    }
});

// 3. Furnace & Blast Furnace recipes
writeJson('mekanism_complex_industries/recipes/smelting/solid_crude_oil_from_ore.json', {
    type: "minecraft:smelting",
    ingredient: { item: "mekanism_complex_industries:solid_crude_oil_ore" },
    result: "mekanism_complex_industries:solid_crude_oil",
    experience: 0.7,
    cookingtime: 200
});

writeJson('mekanism_complex_industries/recipes/smelting/solid_crude_oil_from_deepslate_ore.json', {
    type: "minecraft:smelting",
    ingredient: { item: "mekanism_complex_industries:deepslate_solid_crude_oil_ore" },
    result: "mekanism_complex_industries:solid_crude_oil",
    experience: 0.7,
    cookingtime: 200
});

writeJson('mekanism_complex_industries/recipes/blasting/solid_crude_oil_from_ore.json', {
    type: "minecraft:blasting",
    ingredient: { item: "mekanism_complex_industries:solid_crude_oil_ore" },
    result: "mekanism_complex_industries:solid_crude_oil",
    experience: 0.7,
    cookingtime: 100
});

writeJson('mekanism_complex_industries/recipes/blasting/solid_crude_oil_from_deepslate_ore.json', {
    type: "minecraft:blasting",
    ingredient: { item: "mekanism_complex_industries:deepslate_solid_crude_oil_ore" },
    result: "mekanism_complex_industries:solid_crude_oil",
    experience: 0.7,
    cookingtime: 100
});

// 4. Mekanism PRC Reaction Recipes: Dual Track Ecology!
// Route 1 (Solid high yield 2x): Solid Oil + Lava (100mB) + Steam (100mB) -> Mud + Dense Crude Oil Gas (200mB)
writeJson('mekanism_complex_industries/recipes/reaction/dense_crude_oil_from_solid.json', {
    type: "mekanism:reaction",
    duration: 100,
    itemInput: {
        ingredient: {
            item: "mekanism_complex_industries:solid_crude_oil"
        }
    },
    fluidInput: {
        amount: 100,
        tag: "minecraft:lava"
    },
    gasInput: {
        amount: 100,
        gas: "mekanism:steam"
    },
    itemOutput: {
        item: "minecraft:mud"
    },
    gasOutput: {
        amount: 200,
        gas: "mekanism_complex_industries:dense_crude_oil"
    }
});

// Route 2 (Direct fluid 1x): Sand + Crude Oil Fluid (100mB) + Steam (100mB) -> Mud + Dense Crude Oil Gas (100mB)
writeJson('mekanism_complex_industries/recipes/reaction/dense_crude_oil_from_fluid.json', {
    type: "mekanism:reaction",
    duration: 100,
    itemInput: {
        ingredient: {
            tag: "minecraft:sand"
        }
    },
    fluidInput: {
        amount: 100,
        fluid: "mekanism_complex_industries:crude_oil"
    },
    gasInput: {
        amount: 100,
        gas: "mekanism:steam"
    },
    itemOutput: {
        item: "minecraft:mud"
    },
    gasOutput: {
        amount: 100,
        gas: "mekanism_complex_industries:dense_crude_oil"
    }
});

// 5. Tags
writeJson('forge/tags/fluids/crude_oil.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:crude_oil",
        "mekanism_complex_industries:flowing_crude_oil"
    ]
});

writeJson('forge/tags/fluids/oil.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:crude_oil",
        "mekanism_complex_industries:flowing_crude_oil"
    ]
});

writeJson('forge/tags/items/ores/crude_oil.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:solid_crude_oil_ore",
        "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
    ]
});

writeJson('forge/tags/items/ores/oil.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:solid_crude_oil_ore",
        "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
    ]
});

writeJson('forge/tags/blocks/ores/crude_oil.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:solid_crude_oil_ore",
        "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
    ]
});

writeJson('forge/tags/blocks/ores/oil.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:solid_crude_oil_ore",
        "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
    ]
});

writeJson('minecraft/tags/blocks/mineable/pickaxe.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:solid_crude_oil_ore",
        "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
    ]
});

writeJson('minecraft/tags/blocks/needs_iron_tool.json', {
    replace: false,
    values: [
        "mekanism_complex_industries:solid_crude_oil_ore",
        "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
    ]
});

// 6. World Generation
// Configured feature: Lake
writeJson('mekanism_complex_industries/worldgen/configured_feature/crude_oil_lake.json', {
    type: "minecraft:lake",
    config: {
        fluid: {
            type: "minecraft:simple_state_provider",
            state: {
                Name: "mekanism_complex_industries:crude_oil",
                Properties: {
                    level: "0"
                }
            }
        },
        barrier: {
            type: "minecraft:simple_state_provider",
            state: {
                Name: "minecraft:mud"
            }
        }
    }
});

// Placed feature: Lake
writeJson('mekanism_complex_industries/worldgen/placed_feature/crude_oil_lake.json', {
    feature: "mekanism_complex_industries:crude_oil_lake",
    placement: [
        {
            type: "minecraft:rarity_filter",
            chance: 60
        },
        {
            type: "minecraft:in_square"
        },
        {
            type: "minecraft:height_range",
            height: {
                type: "minecraft:uniform",
                min_inclusive: { "absolute": 40 },
                max_inclusive: { "absolute": 100 }
            }
        },
        {
            type: "minecraft:biome"
        }
    ]
});

// Configured feature: Ore
writeJson('mekanism_complex_industries/worldgen/configured_feature/ore_solid_crude_oil.json', {
    type: "minecraft:ore",
    config: {
        size: 7,
        discard_chance_on_air_exposure: 0.0,
        targets: [
            {
                target: {
                    predicate_type: "minecraft:tag_match",
                    tag: "minecraft:stone_ore_replaceables"
                },
                state: {
                    Name: "mekanism_complex_industries:solid_crude_oil_ore"
                }
            },
            {
                target: {
                    predicate_type: "minecraft:tag_match",
                    tag: "minecraft:deepslate_ore_replaceables"
                },
                state: {
                    Name: "mekanism_complex_industries:deepslate_solid_crude_oil_ore"
                }
            }
        ]
    }
});

// Placed feature: Ore
writeJson('mekanism_complex_industries/worldgen/placed_feature/ore_solid_crude_oil.json', {
    feature: "mekanism_complex_industries:ore_solid_crude_oil",
    placement: [
        {
            type: "minecraft:count",
            count: 4
        },
        {
            type: "minecraft:in_square"
        },
        {
            type: "minecraft:height_range",
            height: {
                type: "minecraft:uniform",
                min_inclusive: { "absolute": -40 },
                max_inclusive: { "absolute": 48 }
            }
        },
        {
            type: "minecraft:biome"
        }
    ]
});

// Forge Biome Modifiers
writeJson('mekanism_complex_industries/forge/biome_modifier/crude_oil_lake.json', {
    type: "forge:add_features",
    biomes: "#minecraft:is_overworld",
    features: "mekanism_complex_industries:crude_oil_lake",
    step: "lakes"
});

writeJson('mekanism_complex_industries/forge/biome_modifier/ore_solid_crude_oil.json', {
    type: "forge:add_features",
    biomes: "#minecraft:is_overworld",
    features: "mekanism_complex_industries:ore_solid_crude_oil",
    step: "underground_ores"
});

console.log('All data, recipes, tags, and worldgen JSONs generated successfully!');
