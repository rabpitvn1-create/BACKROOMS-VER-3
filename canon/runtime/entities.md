# Entity canon projection source

> EDITABLE SOURCE. Runtime output: `android-apk/app/src/main/assets/knowledge/entity_encounters.json`. Run `python3 tools/canon.py generate` after editing.

```json
{
  "schemaVersion": 1,
  "rollMode": "independent_per_entity",
  "source": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
  "rules": {
    "minimumRatePercent": 3,
    "maximumRatePercent": 3.5,
    "sharedPool": false,
    "maxActiveEncounters": 1,
    "legacyOverlayOnlyKeys": [
      "jane_the_killer",
      "slenderman"
    ],
    "roamingAllLevels": true,
    "canonLevelRestrictionsApplied": false,
    "levelMetadataMeaning": "reference_only_not_runtime_eligibility",
    "entitySkillProcPolicy": "Each skill rolls independently once per valid Entity response; if none proc, use the normal basic attack.",
    "treasureMaximumRatePercent": 4,
    "treasurePriorityBeforeStandardPool": true
  },
  "entities": [
    {
      "key": "hound",
      "name": "Hound",
      "ratePercent": 3.5,
      "levels": [
        1,
        2,
        4,
        5
      ],
      "canon": "Fast aggressive four-limbed predator. Hunts humans, often in groups, and exploits isolation, injury and constrained routes.",
      "autoProcSkills": [
        {
          "name": "Dead Bite",
          "damagePercent": 120,
          "procPercent": 35
        },
        {
          "name": "Rending Pounce",
          "damagePercent": 115,
          "procPercent": 32
        },
        {
          "name": "Pack Maul",
          "damagePercent": 110,
          "procPercent": 34
        }
      ],
      "_canon": {
        "id": "entity.hound.foundation",
        "owner": "world:entity:hound",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:hound"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.hound",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:hound",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.hound.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "clump",
      "name": "Clump",
      "ratePercent": 3.5,
      "levels": [
        1,
        2
      ],
      "canon": "Mass of limbs and muscle that ambushes from dark recesses, technical gaps and crawlspaces and drags humans out of formation.",
      "autoProcSkills": [
        {
          "name": "Grasping Crush",
          "damagePercent": 120,
          "procPercent": 34
        },
        {
          "name": "Limb Barrage",
          "damagePercent": 115,
          "procPercent": 35
        },
        {
          "name": "Drag Down",
          "damagePercent": 110,
          "procPercent": 31
        }
      ],
      "_canon": {
        "id": "entity.clump.foundation",
        "owner": "world:entity:clump",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:clump"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.clump",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:clump",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.clump.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "duller",
      "name": "Duller",
      "ratePercent": 3.25,
      "levels": [
        1
      ],
      "canon": "Distorted humanoid Entity that watches before approaching and exploits concrete-column blind spots.",
      "autoProcSkills": [
        {
          "name": "Blindside Strike",
          "damagePercent": 125,
          "procPercent": 33
        },
        {
          "name": "Distorted Lunge",
          "damagePercent": 115,
          "procPercent": 35
        },
        {
          "name": "Column Ambush",
          "damagePercent": 120,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.duller.foundation",
        "owner": "world:entity:duller",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:duller"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.duller",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:duller",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.duller.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "deathmoth",
      "name": "Deathmoth",
      "ratePercent": 3.4,
      "levels": [
        1,
        3,
        4
      ],
      "canon": "Large moth-like predator that uses dark ceilings and technical recesses and reacts to light and movement.",
      "autoProcSkills": [
        {
          "name": "Ceiling Dive",
          "damagePercent": 110,
          "procPercent": 31
        },
        {
          "name": "Wing Strike",
          "damagePercent": 115,
          "procPercent": 32
        },
        {
          "name": "Dark Recess Ambush",
          "damagePercent": 120,
          "procPercent": 23
        }
      ],
      "_canon": {
        "id": "entity.deathmoth.foundation",
        "owner": "world:entity:deathmoth",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:deathmoth"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.deathmoth",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:deathmoth",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.deathmoth.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "hostile_faceling",
      "name": "Hostile Faceling",
      "ratePercent": 3.2,
      "levels": [
        1
      ],
      "canon": "Human-shaped faceless predator. Apparent human-like behavior only reduces vigilance before it hunts.",
      "autoProcSkills": [
        {
          "name": "Sudden Grasp",
          "damagePercent": 110,
          "procPercent": 35
        },
        {
          "name": "Close-Quarters Strike",
          "damagePercent": 115,
          "procPercent": 26
        },
        {
          "name": "Predatory Lunge",
          "damagePercent": 120,
          "procPercent": 27
        }
      ],
      "_canon": {
        "id": "entity.hostile_faceling.foundation",
        "owner": "world:entity:hostile_faceling",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:hostile_faceling"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.hostile_faceling",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:hostile_faceling",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.hostile_faceling.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "false_puddle",
      "name": "False Puddle",
      "ratePercent": 3.15,
      "levels": [
        1
      ],
      "canon": "Predator mimicking a flat puddle of Almond Water. It reacts to human footsteps and opens a toothed mouth when prey steps on it.",
      "autoProcSkills": [
        {
          "name": "Toothed Snap",
          "damagePercent": 110,
          "procPercent": 31
        },
        {
          "name": "Footstep Ambush",
          "damagePercent": 115,
          "procPercent": 27
        },
        {
          "name": "Puddle Bite",
          "damagePercent": 120,
          "procPercent": 25
        }
      ],
      "_canon": {
        "id": "entity.false_puddle.foundation",
        "owner": "world:entity:false_puddle",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:false_puddle"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.false_puddle",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:false_puddle",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.false_puddle.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "paintings",
      "name": "Paintings",
      "ratePercent": 3.1,
      "levels": [
        1
      ],
      "canon": "Predatory paintings whose depicted beings can watch, reach beyond the surface and pull victims into the image.",
      "autoProcSkills": [
        {
          "name": "Canvas Reach",
          "damagePercent": 110,
          "procPercent": 33
        },
        {
          "name": "Frame Ambush",
          "damagePercent": 115,
          "procPercent": 27
        },
        {
          "name": "Pulling Grasp",
          "damagePercent": 120,
          "procPercent": 21
        }
      ],
      "_canon": {
        "id": "entity.paintings.foundation",
        "owner": "world:entity:paintings",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:paintings"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.paintings",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:paintings",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.paintings.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "smiler",
      "name": "Smiler",
      "ratePercent": 3.4,
      "levels": [
        2
      ],
      "canon": "Darkness-using predator seen mainly as a bright face-like feature. It can pressure prey into choosing a bad route rather than charging immediately.",
      "autoProcSkills": [
        {
          "name": "Darkness Lunge",
          "damagePercent": 110,
          "procPercent": 32
        },
        {
          "name": "Blindside Strike",
          "damagePercent": 115,
          "procPercent": 26
        },
        {
          "name": "Shadow Bite",
          "damagePercent": 120,
          "procPercent": 23
        }
      ],
      "_canon": {
        "id": "entity.smiler.foundation",
        "owner": "world:entity:smiler",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:smiler"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.smiler",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:smiler",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.smiler.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "skin-stealer",
      "name": "Skin-Stealer",
      "ratePercent": 3.25,
      "levels": [
        2,
        3,
        4,
        5
      ],
      "canon": "Human-mimicking predator capable of deep disguise and long infiltration. Human appearance never proves a contact is human.",
      "autoProcSkills": [
        {
          "name": "Disguised Strike",
          "damagePercent": 110,
          "procPercent": 32
        },
        {
          "name": "Close-Range Slash",
          "damagePercent": 115,
          "procPercent": 29
        },
        {
          "name": "Ambush Lunge",
          "damagePercent": 120,
          "procPercent": 21
        }
      ],
      "_canon": {
        "id": "entity.skin-stealer.foundation",
        "owner": "world:entity:skin-stealer",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:skin-stealer"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.skin-stealer",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:skin-stealer",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.skin-stealer.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "predatory_window",
      "name": "Predatory Window",
      "ratePercent": 3.1,
      "levels": [
        2,
        4,
        5
      ],
      "canon": "A window-like predatory manifestation in impossible locations that lures humans close and can reach through an unreliable apparent space.",
      "autoProcSkills": [
        {
          "name": "Window Reach",
          "damagePercent": 110,
          "procPercent": 34
        },
        {
          "name": "Threshold Grasp",
          "damagePercent": 115,
          "procPercent": 28
        },
        {
          "name": "Close-Range Pull",
          "damagePercent": 120,
          "procPercent": 25
        }
      ],
      "_canon": {
        "id": "entity.predatory_window.foundation",
        "owner": "world:entity:predatory_window",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:predatory_window"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.predatory_window",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:predatory_window",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.predatory_window.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "biological_pipeline",
      "name": "Biological Pipeline",
      "ratePercent": 3,
      "levels": [
        2
      ],
      "canon": "Huge organic/mechanical pipeline-like predator that senses vibration and heat, offers false safe routes, traps retreat and can secrete tissue-corrosive alkaline liquid.",
      "autoProcSkills": [
        {
          "name": "Constriction",
          "damagePercent": 110,
          "procPercent": 33
        },
        {
          "name": "Pipeline Crush",
          "damagePercent": 115,
          "procPercent": 30
        },
        {
          "name": "Corrosive Contact",
          "damagePercent": 120,
          "procPercent": 20
        }
      ],
      "_canon": {
        "id": "entity.biological_pipeline.foundation",
        "owner": "world:entity:biological_pipeline",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:biological_pipeline"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.biological_pipeline",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:biological_pipeline",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.biological_pipeline.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "wretch",
      "name": "Wretch",
      "ratePercent": 3.3,
      "levels": [
        3
      ],
      "canon": "Irregular, pain-tolerant predator believed or suspected to have once been human. It tracks prey through sound and scent.",
      "autoProcSkills": [
        {
          "name": "Ragged Swipe",
          "damagePercent": 110,
          "procPercent": 32
        },
        {
          "name": "Pain-Fueled Lunge",
          "damagePercent": 115,
          "procPercent": 27
        },
        {
          "name": "Scent-Tracked Strike",
          "damagePercent": 120,
          "procPercent": 22
        }
      ],
      "_canon": {
        "id": "entity.wretch.foundation",
        "owner": "world:entity:wretch",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:wretch"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.wretch",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:wretch",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.wretch.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "cable_mimic",
      "name": "Cable Mimic",
      "ratePercent": 3.2,
      "levels": [
        3
      ],
      "canon": "Entity that blends into electrical cable bundles, waits motionless, then wraps limbs and can conduct facility current through a victim.",
      "autoProcSkills": [
        {
          "name": "Cable Lash",
          "damagePercent": 110,
          "procPercent": 35
        },
        {
          "name": "Bundle Constriction",
          "damagePercent": 115,
          "procPercent": 29
        },
        {
          "name": "Current-Driven Grip",
          "damagePercent": 120,
          "procPercent": 28
        }
      ],
      "_canon": {
        "id": "entity.cable_mimic.foundation",
        "owner": "world:entity:cable_mimic",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:cable_mimic"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.cable_mimic",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:cable_mimic",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.cable_mimic.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "the_beast_of_level_5",
      "name": "The Beast of Level 5",
      "ratePercent": 3,
      "levels": [
        5
      ],
      "canon": "Apex Level 5 Entity with above-human intelligence that hunts humans through long observation, isolation and manipulation of hotel architecture.",
      "autoProcSkills": [
        {
          "name": "Cornered Strike",
          "damagePercent": 110,
          "procPercent": 33
        },
        {
          "name": "Isolated Prey Ambush",
          "damagePercent": 115,
          "procPercent": 32
        },
        {
          "name": "Hotel Corridor Lunge",
          "damagePercent": 120,
          "procPercent": 21
        }
      ],
      "_canon": {
        "id": "entity.the_beast_of_level_5.foundation",
        "owner": "world:entity:the_beast_of_level_5",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:the_beast_of_level_5"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.the_beast_of_level_5",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:the_beast_of_level_5",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.the_beast_of_level_5.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "hotel_corpse_lure",
      "name": "Hotel Corpse Lure",
      "ratePercent": 3,
      "levels": [
        5
      ],
      "canon": "Level 5 corpse-lure phenomenon or Entity that can preserve heat, voice, movement or useful-looking clues to draw humans closer.",
      "autoProcSkills": [
        {
          "name": "Corpse-Lure Grasp",
          "damagePercent": 110,
          "procPercent": 35
        },
        {
          "name": "Close-Range Ambush",
          "damagePercent": 115,
          "procPercent": 28
        },
        {
          "name": "Sudden Strike",
          "damagePercent": 120,
          "procPercent": 20
        }
      ],
      "_canon": {
        "id": "entity.hotel_corpse_lure.foundation",
        "owner": "world:entity:hotel_corpse_lure",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:hotel_corpse_lure"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.hotel_corpse_lure",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:hotel_corpse_lure",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.hotel_corpse_lure.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "jeff_the_killer",
      "name": "Jeff the Killer",
      "ratePercent": 3,
      "levels": [
        0,
        1,
        2,
        3,
        4,
        5,
        6
      ],
      "canon": "Very rare roaming humanoid predator permitted as an incursion across Levels 0–6. It hunts humans and may conduct long tactical observation.",
      "autoProcSkills": [
        {
          "name": "Stalking Slash",
          "damagePercent": 110,
          "procPercent": 34
        },
        {
          "name": "Close-Range Lunge",
          "damagePercent": 115,
          "procPercent": 29
        },
        {
          "name": "Tactical Ambush",
          "damagePercent": 120,
          "procPercent": 23
        }
      ],
      "_canon": {
        "id": "entity.jeff_the_killer.foundation",
        "owner": "world:entity:jeff_the_killer",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:jeff_the_killer"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.jeff_the_killer",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:jeff_the_killer",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.jeff_the_killer.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "async_rifleman",
      "name": "ASYNC Rifleman",
      "faction": "ASYNC",
      "ratePercent": 3,
      "levels": [
        0,
        1,
        2,
        3,
        4,
        5,
        6
      ],
      "canon": "Hostile ASYNC hazmat rifle operator. Uses disciplined mid-range bursts, cover and coordinated crossfire; avoids reckless charges when a firing position is available.",
      "autoProcSkills": [
        {
          "name": "Controlled Burst",
          "damagePercent": 110,
          "procPercent": 32
        },
        {
          "name": "Cover Fire",
          "damagePercent": 115,
          "procPercent": 32
        },
        {
          "name": "Crossfire Burst",
          "damagePercent": 120,
          "procPercent": 28
        }
      ],
      "_canon": {
        "id": "entity.async_rifleman.foundation",
        "owner": "world:entity:async_rifleman",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:async_rifleman"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.async_rifleman",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:async_rifleman",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.async_rifleman.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "copx",
      "name": "CopX",
      "ratePercent": 3,
      "levels": [
        0,
        1,
        2,
        3,
        4,
        5,
        6
      ],
      "canon": "CopX is a corroded late-1980s security automaton, its ivory armor split around exposed servos and a handgun still held in a rigid right arm. Broken evacuation orders leak from its speaker before it identifies moving humans as intruders. It holds long sightlines, fires short measured bursts, then turns stiffly toward movement; cover and sudden changes of angle can break its aim. Its ammunition and sensors are finite, its joints can jam, and it cannot teleport, see through walls or enforce real authority. Sightings since 1987 do not prove that every CopX is the same machine. It is hostile to humans; apparent warnings are degraded threat signals, never a promise of rescue. Defeating one chassis ends that encounter, not necessarily every future sighting.",
      "autoProcSkills": [
        {
          "name": "Static Burst",
          "damagePercent": 110,
          "procPercent": 30
        },
        {
          "name": "Servo Pivot",
          "damagePercent": 115,
          "procPercent": 25
        },
        {
          "name": "Last Directive",
          "damagePercent": 120,
          "procPercent": 20
        }
      ],
      "_canon": {
        "id": "entity.copx.foundation",
        "owner": "world:entity:copx",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:copx"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.copx",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:copx",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.copx.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "tam_ma_cao_minh",
      "name": "Tâm Ma Cao Minh",
      "spawnClass": "treasure",
      "ratePercent": 4,
      "levels": [
        0,
        1,
        2,
        3,
        4,
        5,
        6
      ],
      "canon": "A rare hostile mirror-manifestation of Cao Minh. It resembles and echoes Cao Minh but acts as an independent predatory Entity; its exact origin remains unresolved. Treat it as a roaming Treasure Entity, not a resident population.",
      "autoProcSkills": [
        {
          "name": "Tâm Ma Trảm",
          "damagePercent": 120,
          "procPercent": 35
        },
        {
          "name": "Huyết Ảnh Phản Kích",
          "damagePercent": 115,
          "procPercent": 40
        },
        {
          "name": "Ma Hổ Phệ",
          "damagePercent": 110,
          "procPercent": 45
        }
      ],
      "_canon": {
        "id": "entity.tam_ma_cao_minh.foundation",
        "owner": "world:entity:tam_ma_cao_minh",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:tam_ma_cao_minh"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.tam_ma_cao_minh",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:tam_ma_cao_minh",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.tam_ma_cao_minh.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "diep_minh",
      "name": "Diệp Minh",
      "asset": "entity/diep_minh.webp",
      "canonRefs": [
        "DIEP-MINH-VIS-01",
        "DIEP-MINH-CAO-REL-01",
        "DIEP-MINH-BACKROOMS-01"
      ],
      "canon": "Diệp Minh là tử địch không đội trời chung của Cao Minh. Hắn từng trực tiếp góp phần vào đại kiếp thảm sát Cao gia, biến cố đã đẩy Cao Minh vào tuyệt vọng và trở thành một nguyên nhân quyết định khiến hắn bước vào ma đạo; Diệp Minh không mặc định là chủ mưu duy nhất. Ngoại hình khóa theo entity/diep_minh.webp: nam tử trẻ tóc đen, pháp giáp đen-vàng; nửa phải còn hình người, nửa trái dị hóa thành ma thể đen-đỏ với hồng quang, tua nhánh và vuốt; tay phải cầm trường kiếm tỏa kim quang, quanh thân có phù vàng chữ đỏ và họa tiết âm dương/bát quái. Diệp Minh nguyên bản đã chết trước các lần xuất hiện trong Backrooms. Thực thể hiện tại nhận ra Cao Minh và giữ ký ức cốt lõi về huyết cừu, nhưng việc nó là tàn hồn, bản sao, tái tạo hay một cơ chế khác của Backrooms vẫn OPEN. Một manifestation có thể bị tiêu diệt nhưng không bị xóa vĩnh viễn; không có thời gian hoặc địa điểm tái xuất cố định.",
      "autoProcSkills": [
        {
          "name": "Golden Sword Slash",
          "damagePercent": 110,
          "procPercent": 32
        },
        {
          "name": "Demonic Claw",
          "damagePercent": 115,
          "procPercent": 32
        },
        {
          "name": "Sword-Claw Assault",
          "damagePercent": 120,
          "procPercent": 21
        }
      ],
      "ratePercent": 3.5,
      "levels": [
        0,
        1,
        2,
        3,
        4,
        5,
        6
      ],
      "_canon": {
        "id": "entity.diep_minh.foundation",
        "owner": "world:entity:diep_minh",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:diep_minh"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.diep_minh",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:diep_minh",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.diep_minh.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_01",
      "name": "Nhân viên Mực Đen",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 1",
      "canon": "Hui's Family source boss of Level 1. Source environment: Văn phòng hành chính vô tận, giấy dán tường vàng úa, thảm nỉ ẩm và hồ sơ ghi lại hành động vừa xảy ra. Source rule: Bản đồ không đáng tin; không mở cửa đỏ khi có tiếng gõ phía sau, vì tiếng gõ có thể phát ra từ chính phía người mở. Source threat descriptions: Nhân viên Mực Đen bò ra từ ngăn kéo, gầm bàn và vết mực; Nạn nhân bị biến thành hồ sơ. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Ink Drawer Strike",
          "damagePercent": 126,
          "procPercent": 32
        },
        {
          "name": "Dossier Bind",
          "damagePercent": 136,
          "procPercent": 25
        },
        {
          "name": "Black Ink Sweep",
          "damagePercent": 116,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_01.foundation",
        "owner": "world:entity:huis_boss_01",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_01"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_01",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_01",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_01.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_02",
      "name": "Người Gõ Cửa",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 2",
      "canon": "Hui's Family source boss of Level 2. Source environment: Hành lang khách sạn hẹp với hàng triệu cửa đỏ có số thứ tự liên tục thay đổi. Source rule: Người Gõ Cửa chỉ tồn tại khi cửa khép; tay nắm nóng chỉ an toàn trước khi bị chạm. Source threat descriptions: Cửa tự mở đồng loạt; Người Gõ Cửa xuất hiện gần hơn sau mỗi lần đóng cửa. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Closed Door Rush",
          "damagePercent": 127,
          "procPercent": 32
        },
        {
          "name": "Handle Burn",
          "damagePercent": 137,
          "procPercent": 25
        },
        {
          "name": "Numberless Threshold",
          "damagePercent": 117,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_02.foundation",
        "owner": "world:entity:huis_boss_02",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_02"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_02",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_02",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_02.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_03",
      "name": "Hành Khách Trễ Giờ",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 3",
      "canon": "Hui's Family source boss of Level 3. Source environment: Nhà ga mái kính không có đường ray, bảng giờ ghi những nơi không tồn tại và một đầu máy vô hình đi xuyên đại sảnh. Source rule: Không trả lời câu hỏi về thời gian bằng bất kỳ con số nào; người trả lời sẽ bị nhận diện là chuyến tàu. Source threat descriptions: Hành Khách Trễ Giờ; Đầu máy vô hình; Bị đánh dấu là chuyến tàu. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Late Arrival Charge",
          "damagePercent": 128,
          "procPercent": 32
        },
        {
          "name": "Platform Slam",
          "damagePercent": 138,
          "procPercent": 25
        },
        {
          "name": "Timetable Pursuit",
          "damagePercent": 118,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_03.foundation",
        "owner": "world:entity:huis_boss_03",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_03"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_03",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_03",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_03.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_04",
      "name": "Người Hầu Không Lưng",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 4",
      "canon": "Hui's Family source boss of Level 4. Source environment: Khách sạn Belle Époque vô tận với phòng đã chuẩn bị sẵn cho người chưa từng đến. Source rule: Ngủ trong phòng khóa kín sẽ tỉnh ở hành lang; ngủ giữa hành lang sẽ tỉnh trong phòng bị khóa từ ngoài. Source threat descriptions: Người Hầu Không Lưng; Sinh vật chui ra từ khoảng rỗng sau thân chúng. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Hollow Back Grasp",
          "damagePercent": 129,
          "procPercent": 32
        },
        {
          "name": "Corridor Pull",
          "damagePercent": 139,
          "procPercent": 25
        },
        {
          "name": "Service Door Ambush",
          "damagePercent": 119,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_04.foundation",
        "owner": "world:entity:huis_boss_04",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_04"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_04",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_04",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_04.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_05",
      "name": "Thợ Máy Rỗng",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 5",
      "canon": "Hui's Family source boss of Level 5. Source environment: Nhà máy gạch đỏ co giãn như lồng ngực; máy móc chỉ vận hành máy móc khác mà không tạo sản phẩm. Source rule: Mỗi nhịp thở ra phủ hơi nóng kín hành lang; tiếng động cho phép Thợ Máy Rỗng tạo công cụ săn mồi từ cơ thể chúng. Source threat descriptions: Thợ Máy Rỗng; Hình người bị ép khỏi van hơi và nồi hơi khóa. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Steam Hammer",
          "damagePercent": 130,
          "procPercent": 32
        },
        {
          "name": "Boiler Hook",
          "damagePercent": 140,
          "procPercent": 25
        },
        {
          "name": "Pressure Release",
          "damagePercent": 120,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_05.foundation",
        "owner": "world:entity:huis_boss_05",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_05"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_05",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_05",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_05.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_06",
      "name": "Y Tá Màn Trắng",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 6",
      "canon": "Hui's Family source boss of Level 6. Source environment: Bệnh viện gạch trắng có giường lõm, chuông gọi từ phòng bị xây kín và bệnh án về những chứng bệnh không thể tồn tại. Source rule: Người bị thương ít bị săn hơn người khỏe; tự gây thương tích khiến bệnh viện coi người đó là nhân viên. Source threat descriptions: Y Tá Màn Trắng sửa cơ thể cho khớp bệnh án; Bệnh nhân vô hình. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Ward Clamp",
          "damagePercent": 131,
          "procPercent": 32
        },
        {
          "name": "Surgical Reach",
          "damagePercent": 141,
          "procPercent": 25
        },
        {
          "name": "White Veil Strike",
          "damagePercent": 121,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_06.foundation",
        "owner": "world:entity:huis_boss_06",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_06"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_06",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_06",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_06.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_07",
      "name": "Diễn Viên Chưa Ra Đời",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 7",
      "canon": "Hui's Family source boss of Level 7. Source environment: Nhà hát không có sân khấu, mọi ghế quay về một hố trống và dàn nhạc vô hình luôn chơi sai một nốt. Source rule: Mỗi nốt sai tạo thêm khán giả; gây tiếng động khiến toàn khán phòng vỗ tay và thực thể bò qua ghế. Source threat descriptions: Khán Giả Nhìn Ngược; Diễn Viên Chưa Ra Đời mang gương mặt từ ký ức người nhìn. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Mask Change",
          "damagePercent": 132,
          "procPercent": 32
        },
        {
          "name": "Curtain Drop",
          "damagePercent": 142,
          "procPercent": 25
        },
        {
          "name": "Encore Lunge",
          "damagePercent": 122,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_07.foundation",
        "owner": "world:entity:huis_boss_07",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_07"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_07",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_07",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_07.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_08",
      "name": "Thủ Thư Không Miệng",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 8",
      "canon": "Hui's Family source boss of Level 8. Source environment: Kho hồ sơ vô tận nơi mực bò khỏi trang và việc đọc xóa từ, khái niệm hoặc ký ức tương ứng. Source rule: Đọc làm mất ký ức; đốt tài liệu trả chữ lại nhưng chữ xuất hiện trên tường, da hoặc bên trong mí mắt. Source threat descriptions: Con dấu xóa tên, kỹ năng, ký ức hoặc giác quan; Mất khái niệm về đường thoát. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Silent Seal",
          "damagePercent": 133,
          "procPercent": 32
        },
        {
          "name": "Archive Crush",
          "damagePercent": 143,
          "procPercent": 25
        },
        {
          "name": "Blank Page Strike",
          "damagePercent": 123,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_08.foundation",
        "owner": "world:entity:huis_boss_08",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_08"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_08",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_08",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_08.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_09",
      "name": "Người Đi Dạo",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 9",
      "canon": "Hui's Family source boss of Level 9. Source environment: Thành phố thương mại dưới mái kính, mưa bay ngược và biển hiệu đổi ngôn ngữ sau mỗi lần chớp mắt. Source rule: Bóng đổ có thể tách khỏi chủ thể, mọc cơ thể và tiếp tục tồn tại độc lập. Source threat descriptions: Người Đi Dạo; Ma-nơ-canh; Bàn tay thò xuống từ mái kính. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Glass Canopy Dive",
          "damagePercent": 134,
          "procPercent": 32
        },
        {
          "name": "Reflection Step",
          "damagePercent": 144,
          "procPercent": 25
        },
        {
          "name": "Sidewalk Ambush",
          "damagePercent": 124,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_09.foundation",
        "owner": "world:entity:huis_boss_09",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_09"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_09",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_09",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_09.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_10",
      "name": "Giám Thị Dài Tay",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 10",
      "canon": "Hui's Family source boss of Level 10. Source environment: Trường học nằm trong đêm bất biến; bảng đen tự viết bài học về người đang có mặt. Source rule: Giám thị xuất hiện khi có người chạy, nói lớn hoặc mở cửa không được phép, nhưng không ai biết cửa nào được phép. Source threat descriptions: Học Sinh Trong Tường; Giám Thị Dài Tay. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Long Arm Sweep",
          "damagePercent": 135,
          "procPercent": 32
        },
        {
          "name": "Bell Discipline",
          "damagePercent": 145,
          "procPercent": 25
        },
        {
          "name": "Hallway Seizure",
          "damagePercent": 125,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_10.foundation",
        "owner": "world:entity:huis_boss_10",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_10"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_10",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_10",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_10.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_11",
      "name": "Thủy Thủ Trương Nở",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 11",
      "canon": "Hui's Family source boss of Level 11. Source environment: Tàu viễn dương bị nhét vào hành lang đá, cửa sổ nhìn ra đại dương dựng đứng và nước rơi từ trần. Source rule: Không gian rộng làm thủy thủ phình lên; khe hẹp ép chúng mỏng như giấy. Source threat descriptions: Thủy Thủ Trương Nở; Người Chết Đuối Khô kéo nạn nhân về mặt nước dưới sàn. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Swollen Hull Slam",
          "damagePercent": 136,
          "procPercent": 32
        },
        {
          "name": "Deck Grip",
          "damagePercent": 146,
          "procPercent": 25
        },
        {
          "name": "Bulkhead Crush",
          "damagePercent": 126,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_11.foundation",
        "owner": "world:entity:huis_boss_11",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_11"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_11",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_11",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_11.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_12",
      "name": "Phu Lò",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 12",
      "canon": "Hui's Family source boss of Level 12. Source environment: Cống, hầm lò và đường than đan nhau trong không khí nóng đặc; xe goòng chở giày, răng người và cửa đỏ. Source rule: Lửa xua phần lớn quái vật nhưng làm đường hầm dài thêm. Source threat descriptions: Phu Lò chỉ có mắt trong bóng tối; Sinh vật trồi khỏi than, nước và kim loại nóng. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Coal Shovel Blow",
          "damagePercent": 137,
          "procPercent": 32
        },
        {
          "name": "Furnace Grip",
          "damagePercent": 147,
          "procPercent": 25
        },
        {
          "name": "Tunnel Rush",
          "damagePercent": 127,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_12.foundation",
        "owner": "world:entity:huis_boss_12",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_12"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_12",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_12",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_12.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_13",
      "name": "Nhà Phát Minh",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 13",
      "canon": "Hui's Family source boss of Level 13. Source environment: Khu triển lãm về một thế kỷ mới không bao giờ đến, đầy máy móc có hình dạng hợp lý nhưng chức năng vô nghĩa. Source rule: Người Tham Quan Bằng Sáp bất động khi nhìn trực diện nhưng chạy trong kính phản chiếu. Source threat descriptions: Người Tham Quan Bằng Sáp; Nhà Phát Minh lắp bộ phận nạn nhân vào máy. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Machine Limb Strike",
          "damagePercent": 138,
          "procPercent": 32
        },
        {
          "name": "Exhibit Clamp",
          "damagePercent": 148,
          "procPercent": 25
        },
        {
          "name": "Clockwork Crush",
          "damagePercent": 128,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_13.foundation",
        "owner": "world:entity:huis_boss_13",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_13"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_13",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_13",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_13.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_14",
      "name": "Thánh Tượng Quay Lưng",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 14",
      "canon": "Hui's Family source boss of Level 14. Source environment: Thánh đường đá đen có đồng hồ chỉ về phía người nhìn và chuông xóa sự kiện khỏi lịch sử cá nhân. Source rule: Không quan sát thì tượng đứng sau lưng; nhìn quá lâu khiến da hóa đá. Source threat descriptions: Tu Sĩ Không Chuông; Thánh Tượng Quay Lưng; Mất ký ức cá nhân. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Stone Gaze",
          "damagePercent": 139,
          "procPercent": 32
        },
        {
          "name": "Backward Step",
          "damagePercent": 149,
          "procPercent": 25
        },
        {
          "name": "Bell Tower Strike",
          "damagePercent": 129,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_14.foundation",
        "owner": "world:entity:huis_boss_14",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_14"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_14",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_14",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_14.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_15",
      "name": "Cư Dân Ban Công",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 15",
      "canon": "Hui's Family source boss of Level 15. Source environment: Thành phố không có mặt đất, gồm căn hộ và ban công chồng lên nhau trong vực sâu vô tận. Source rule: Đi xuống làm thành phố sáng hơn; đi lên chỉ nghe phố xá mà không bao giờ tới mặt đất. Source threat descriptions: Cư Dân Ban Công; Căn hộ bò về phía người hỏi; Quái vật chui từ đồ gia dụng. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Balcony Descent",
          "damagePercent": 140,
          "procPercent": 32
        },
        {
          "name": "Apartment Grasp",
          "damagePercent": 150,
          "procPercent": 25
        },
        {
          "name": "Void Push",
          "damagePercent": 130,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_15.foundation",
        "owner": "world:entity:huis_boss_15",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_15"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_15",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_15",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_15.foundation"
        ],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "huis_boss_16",
      "name": "Đám Đông Chưa Sinh",
      "ratePercent": 3,
      "spawnClass": "boss",
      "tier": "boss",
      "originLevel": "Hui's Family Level 16",
      "canon": "Hui's Family source boss of Level 16. Source environment: Mọi tầng trước bị ép chung vào một kiến trúc; ký ức sai trở thành hành lang và người quen trở thành thực thể. Source rule: Tầng dùng ký ức người sống để xây thêm Backrooms; một bản sao thế giới thật có thể khiến cả nhóm tin rằng đã thoát. Source threat descriptions: Mọi thực thể từ các tầng trước; Đám Đông Chưa Sinh; Bản sao của người quen và thế giới thật. The boss roams all active Backrooms and Hui's Family Levels; origin is reference, not a spawn restriction.",
      "autoProcSkills": [
        {
          "name": "Crowd Surge",
          "damagePercent": 141,
          "procPercent": 32
        },
        {
          "name": "False Memory Strike",
          "damagePercent": 151,
          "procPercent": 25
        },
        {
          "name": "Century Collapse",
          "damagePercent": 131,
          "procPercent": 30
        }
      ],
      "_canon": {
        "id": "entity.huis_boss_16.foundation",
        "owner": "world:entity:huis_boss_16",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "runtime-current",
        "scope": [
          "entity:huis_boss_16"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      },
      "_canonGameplay": {
        "id": "gameplay.entity.huis_boss_16",
        "owner": "core:combat",
        "type": "GAMEPLAY",
        "status": "CURRENT",
        "sourceRef": "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java",
        "revision": "86102042b1194877ec0ec4aaacd0100c38ef9d79",
        "scope": [
          "entity:huis_boss_16",
          "combat"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "repo",
        "sourceAvailability": "AVAILABLE",
        "refs": [
          "entity.huis_boss_16.foundation"
        ],
        "requires": [],
        "core": false
      }
    }
  ],
  "legacyEntities": [
    {
      "key": "jane_the_killer",
      "name": "Jane",
      "autoSpawn": false,
      "autoProcSkills": [
        {
          "name": "Stalking Strike",
          "damagePercent": 110,
          "procPercent": 33
        },
        {
          "name": "Close-Range Slash",
          "damagePercent": 115,
          "procPercent": 27
        },
        {
          "name": "Sudden Lunge",
          "damagePercent": 120,
          "procPercent": 25
        }
      ],
      "_canon": {
        "id": "entity.jane_the_killer.legacy",
        "owner": "world:entity:jane_the_killer",
        "type": "FOUNDATION",
        "status": "LEGACY",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "legacy",
        "scope": [
          "entity:jane_the_killer"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": false
      }
    },
    {
      "key": "slenderman",
      "name": "Slenderman",
      "autoSpawn": false,
      "autoProcSkills": [
        {
          "name": "Tendril Strike",
          "damagePercent": 110,
          "procPercent": 31
        },
        {
          "name": "Tendril Sweep",
          "damagePercent": 115,
          "procPercent": 26
        },
        {
          "name": "Looming Grasp",
          "damagePercent": 120,
          "procPercent": 21
        }
      ],
      "_canon": {
        "id": "entity.slenderman.legacy",
        "owner": "world:entity:slenderman",
        "type": "FOUNDATION",
        "status": "LEGACY",
        "sourceRef": "01_WORLD/entity.md — BACKROOMS-ENTITY-R4 CURRENT / PROJECT CANON",
        "revision": "legacy",
        "scope": [
          "entity:slenderman"
        ],
        "knownBy": "SYSTEM",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": false
      }
    }
  ]
}
```
