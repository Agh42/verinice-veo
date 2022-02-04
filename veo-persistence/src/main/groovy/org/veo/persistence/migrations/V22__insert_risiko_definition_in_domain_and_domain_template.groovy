/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  r
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.persistence.migrations

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

import groovy.sql.Sql

class V22__insert_risiko_definition_in_domain_and_domain_template extends BaseJavaMigration {

    @Override
    void migrate(Context context) throws Exception {
        new Sql(context.connection).execute("""

    update domaintemplate set risk_definitions = ${riskDefinitions}::jsonb  ;

    update domain set risk_definitions = ${riskDefinitions}::jsonb  ;

""")
    }

    String riskDefinitions = '''
{
    "id": {
        "id": "id",
        "probability": {
            "id": "Prob",
            "name": "prop-1",
            "abbreviation": "pro-name-1",
            "description": "",
            "levels": [
                {
                    "ordinalValue": 0,
                    "name": "selten",
                    "abbreviation": "1",
                    "description": "Ereignis könnte nach heutigem Kenntnisstand höchstens alle fünf Jahre eintreten.",
                    "htmlColor": "#004643"
                },
                {
                    "ordinalValue": 1,
                    "name": "mittel",
                    "abbreviation": "2",
                    "description": "Ereignis tritt einmal alle fünf Jahre bis einmal im Jahr ein.",
                    "htmlColor": "#004643"
                },
                {
                    "ordinalValue": 2,
                    "name": "häufig",
                    "abbreviation": "3",
                    "description": "Ereignis tritt einmal im Jahr bis einmal pro Monat ein.",
                    "htmlColor": "#004643"
                },
                {
                    "ordinalValue": 3,
                    "name": "sehr häufig",
                    "abbreviation": "4",
                    "description": "Ereignis tritt mehrmals im Monat ein.",
                    "htmlColor": "#004643"
                }
            ]
        },
        "implementationStateDefinition": {
            "id": "Ctr",
            "name": "prop-1",
            "abbreviation": "pro-name-1",
            "description": "",
            "levels": [
                {
                    "ordinalValue": 0,
                    "name": "ja",
                    "abbreviation": "J",
                    "description": "Die Maßnahme ist vollständig umgesetzt.",
                    "htmlColor": "#12AE0F"
                },
                {
                    "ordinalValue": 1,
                    "name": "nein",
                    "abbreviation": "N",
                    "description": "Die Maßnahme ist nicht umgesetzt.",
                    "htmlColor": "#AE0D11"
                },
                {
                    "ordinalValue": 2,
                    "name": "teilweise",
                    "abbreviation": "Tw",
                    "description": "Die Maßnahme ist nicht vollständig umgesetzt.",
                    "htmlColor": "#EDE92F"
                },
                {
                    "ordinalValue": 3,
                    "name": "nicht anwendbar",
                    "abbreviation": "NA",
                    "description": "Die Maßnahme ist für den Betrachtungsgegenstand nicht anwendbar.",
                    "htmlColor": "#49A2ED"
                }
            ]
        },
        "categories": [
            {
                "id": "C",
                "name": "Vertraulichkeit",
                "abbreviation": "c",
                "description": "",
                "valueMatrix": [
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ]
                ],
                "potentialImpacts": [
                    {
                        "ordinalValue": 0,
                        "name": "vernachlässigbar",
                        "abbreviation": "1",
                        "description": "Die Schadensauswirkungen sind gering und können vernachlässigt werden.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 1,
                        "name": "begrenzt",
                        "abbreviation": "2",
                        "description": "Die Schadensauswirkungen sind begrenzt und überschaubar.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 2,
                        "name": "beträchtlich",
                        "abbreviation": "3",
                        "description": "Die Schadensauswirkungen können beträchtlich sein.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 3,
                        "name": "existenzbedrohend",
                        "abbreviation": "4",
                        "description": "Die Schadensauswirkungen können ein existenziell bedrohliches, katastrophales Ausmaß erreichen.",
                        "htmlColor": "#004643"
                    }
                ]
            },
            {
                "id": "I",
                "name": "Integrität",
                "abbreviation": "i",
                "description": "",
                "valueMatrix": [
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ]
                ],
                "potentialImpacts": [
                    {
                        "ordinalValue": 0,
                        "name": "vernachlässigbar",
                        "abbreviation": "1",
                        "description": "Die Schadensauswirkungen sind gering und können vernachlässigt werden.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 1,
                        "name": "begrenzt",
                        "abbreviation": "2",
                        "description": "Die Schadensauswirkungen sind begrenzt und überschaubar.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 2,
                        "name": "beträchtlich",
                        "abbreviation": "3",
                        "description": "Die Schadensauswirkungen können beträchtlich sein.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 3,
                        "name": "existenzbedrohend",
                        "abbreviation": "4",
                        "description": "Die Schadensauswirkungen können ein existenziell bedrohliches, katastrophales Ausmaß erreichen.",
                        "htmlColor": "#004643"
                    }
                ]
            },
            {
                "id": "A",
                "name": "Verfügbarkeit",
                "abbreviation": "a",
                "description": "",
                "valueMatrix": [
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ]
                ],
                "potentialImpacts": [
                    {
                        "ordinalValue": 0,
                        "name": "vernachlässigbar",
                        "abbreviation": "1",
                        "description": "Die Schadensauswirkungen sind gering und können vernachlässigt werden.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 1,
                        "name": "begrenzt",
                        "abbreviation": "2",
                        "description": "Die Schadensauswirkungen sind begrenzt und überschaubar.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 2,
                        "name": "beträchtlich",
                        "abbreviation": "3",
                        "description": "Die Schadensauswirkungen können beträchtlich sein.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 3,
                        "name": "existenzbedrohend",
                        "abbreviation": "4",
                        "description": "Die Schadensauswirkungen können ein existenziell bedrohliches, katastrophales Ausmaß erreichen.",
                        "htmlColor": "#004643"
                    }
                ]
            },
            {
                "id": "R",
                "name": "Belastbarkeit",
                "abbreviation": "r",
                "description": "",
                "valueMatrix": [
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 0,
                            "name": "gering",
                            "abbreviation": "1",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                            "htmlColor": "#A0CF11",
                            "symbolicRisk": "symbolic_risk_1"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ],
                    [
                        {
                            "ordinalValue": 1,
                            "name": "mittel",
                            "abbreviation": "2",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                            "htmlColor": "#FFFF13",
                            "symbolicRisk": "symbolic_risk_2"
                        },
                        {
                            "ordinalValue": 2,
                            "name": "hoch",
                            "abbreviation": "3",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                            "htmlColor": "#FF8E43",
                            "symbolicRisk": "symbolic_risk_3"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        },
                        {
                            "ordinalValue": 3,
                            "name": "sehr hoch",
                            "abbreviation": "4",
                            "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                            "htmlColor": "#FF1212",
                            "symbolicRisk": "symbolic_risk_4"
                        }
                    ]
                ],
                "potentialImpacts": [
                    {
                        "ordinalValue": 0,
                        "name": "vernachlässigbar",
                        "abbreviation": "1",
                        "description": "Die Schadensauswirkungen sind gering und können vernachlässigt werden.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 1,
                        "name": "begrenzt",
                        "abbreviation": "2",
                        "description": "Die Schadensauswirkungen sind begrenzt und überschaubar.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 2,
                        "name": "beträchtlich",
                        "abbreviation": "3",
                        "description": "Die Schadensauswirkungen können beträchtlich sein.",
                        "htmlColor": "#004643"
                    },
                    {
                        "ordinalValue": 3,
                        "name": "existenzbedrohend",
                        "abbreviation": "4",
                        "description": "Die Schadensauswirkungen können ein existenziell bedrohliches, katastrophales Ausmaß erreichen.",
                        "htmlColor": "#004643"
                    }
                ]
            }
        ],
        "riskValues": [
            {
                "ordinalValue": 0,
                "name": "gering",
                "abbreviation": "1",
                "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.",
                "htmlColor": "#A0CF11",
                "symbolicRisk": "symbolic_risk_1"
            },
            {
                "ordinalValue": 1,
                "name": "mittel",
                "abbreviation": "2",
                "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.",
                "htmlColor": "#FFFF13",
                "symbolicRisk": "symbolic_risk_2"
            },
            {
                "ordinalValue": 2,
                "name": "hoch",
                "abbreviation": "3",
                "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.",
                "htmlColor": "#FF8E43",
                "symbolicRisk": "symbolic_risk_3"
            },
            {
                "ordinalValue": 3,
                "name": "sehr hoch",
                "abbreviation": "4",
                "description": "Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.",
                "htmlColor": "#FF1212",
                "symbolicRisk": "symbolic_risk_4"
            }
        ],
        "riskMethod": {
            "impactMethod": "highwatermark",
            "description": "description"
        }
    }
}
'''
}
