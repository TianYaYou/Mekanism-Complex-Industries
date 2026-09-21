const fs = require('fs');
const path = require('path');

const LOOT_DIR = path.resolve(__dirname, '../src/main/resources/data/mekanism_complex_industries/loot_tables/blocks');
fs.mkdirSync(LOOT_DIR, { recursive: true });

function makeOreLootTable(oreBlockId, dropItemId) {
    return {
        type: "minecraft:block",
        pools: [
            {
                rolls: 1.0,
                bonus_rolls: 0.0,
                entries: [
                    {
                        type: "minecraft:alternatives",
                        children: [
                            {
                                type: "minecraft:item",
                                name: oreBlockId,
                                conditions: [
                                    {
                                        condition: "minecraft:match_tool",
                                        predicate: {
                                            enchantments: [
                                                {
                                                    enchantment: "minecraft:silk_touch",
                                                    levels: { min: 1 }
                                                }
                                            ]
                                        }
                                    }
                                ]
                            },
                            {
                                type: "minecraft:item",
                                name: dropItemId,
                                functions: [
                                    {
                                        function: "minecraft:apply_bonus",
                                        enchantment: "minecraft:fortune",
                                        formula: "minecraft:ore_drops"
                                    },
                                    {
                                        function: "minecraft:explosion_decay"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ],
        random_sequence: `mekanism_complex_industries:blocks/${path.basename(oreBlockId)}`
    };
}

fs.writeFileSync(
    path.join(LOOT_DIR, 'solid_crude_oil_ore.json'),
    JSON.stringify(makeOreLootTable('mekanism_complex_industries:solid_crude_oil_ore', 'mekanism_complex_industries:solid_crude_oil'), null, 2)
);

fs.writeFileSync(
    path.join(LOOT_DIR, 'deepslate_solid_crude_oil_ore.json'),
    JSON.stringify(makeOreLootTable('mekanism_complex_industries:deepslate_solid_crude_oil_ore', 'mekanism_complex_industries:solid_crude_oil'), null, 2)
);

console.log('Loot tables generated successfully!');
