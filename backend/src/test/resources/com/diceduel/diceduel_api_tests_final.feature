Feature: DiceDuel API - Full REST Operations Test Suite

  Background:
    * url 'http://localhost:3001'
    * header Accept = 'application/json'
    * header Content-Type = 'application/json'
    * def uuid = function(){ return java.util.UUID.randomUUID() + '' }

  Scenario: Players - full CRUD, stats, validation and delete verification
    Given path '/api/players'
    When method GET
    Then status 200
    And match response == '#array'

    Given path '/api/players', 'missing-player'
    When method GET
    Then status 404
    And match response.status == 404

    Given path '/api/players'
    And request {}
    When method POST
    Then status 400

    Given path '/api/players'
    And request { name: 'Karate Player' }
    When method POST
    Then status 201
    And match response.id == '#string'
    And match response.name == 'Karate Player'
    * def playerId = response.id

    Given path '/api/players', playerId
    When method GET
    Then status 200
    And match response.id == playerId

    Given path '/api/players', playerId, 'stats'
    When method GET
    Then status 200
    And match response.wins == 0
    And match response.losses == 0

    Given path '/api/players', playerId
    And request { name: 'Incomplete Player' }
    When method PUT
    Then status 400

    Given path '/api/players', playerId
    And request { name: 'Karate Player Full', hearts: 10, tokens: 2 }
    When method PUT
    Then status 200
    And match response.name == 'Karate Player Full'
    And match response.hearts == 10
    And match response.tokens == 2

    Given path '/api/players', playerId
    And request { hearts: -1 }
    When method PATCH
    Then status 400

    Given path '/api/players', playerId
    And request { tokens: 6 }
    When method PATCH
    Then status 200
    And match response.tokens == 6

    Given path '/api/players', playerId
    When method GET
    Then status 200
    And match response.tokens == 6

    Given path '/api/players', playerId
    When method DELETE
    Then status 204

    Given path '/api/players', playerId
    When method GET
    Then status 404

  Scenario: Abilities - full CRUD, duplicate conflict and validation
    Given path '/api/abilities'
    When method GET
    Then status 200
    And match response == '#array'

    Given path '/api/abilities', 'missing-ability'
    When method GET
    Then status 404

    Given path '/api/abilities'
    And request { name: 'Invalid Ability' }
    When method POST
    Then status 400

    * def abilityId = 'karate-ability-' + uuid()
    Given path '/api/abilities'
    And request { id: '#(abilityId)', name: 'Karate Ability', cost: 2 }
    When method POST
    Then status 201
    And match response.id == abilityId
    And match response.name == 'Karate Ability'

    Given path '/api/abilities'
    And request { id: '#(abilityId)', name: 'Duplicate Ability', cost: 1 }
    When method POST
    Then status 409
    And match response.status == 409

    Given path '/api/abilities', abilityId
    When method GET
    Then status 200
    And match response.id == abilityId

    Given path '/api/abilities', abilityId
    And request { name: 'Incomplete Ability' }
    When method PUT
    Then status 400

    Given path '/api/abilities', abilityId
    And request { name: 'Karate Ability Full', cost: 4 }
    When method PUT
    Then status 200
    And match response.name == 'Karate Ability Full'
    And match response.cost == 4

    Given path '/api/abilities', abilityId
    And request { cost: -1 }
    When method PATCH
    Then status 400

    Given path '/api/abilities', abilityId
    And request { cost: 1 }
    When method PATCH
    Then status 200
    And match response.cost == 1

    Given path '/api/abilities', abilityId
    When method DELETE
    Then status 204

    Given path '/api/abilities', abilityId
    When method GET
    Then status 404

  Scenario: Matches and rounds - lobby, state, dice actions, resolve and replay
    Given path '/api/players'
    And request { name: 'Karate Host' }
    When method POST
    Then status 201
    * def hostId = response.id

    Given path '/api/players'
    And request { name: 'Karate Guest' }
    When method POST
    Then status 201
    * def guestId = response.id

    Given path '/api/matches'
    And request { hostPlayerId: 'missing-player', maxPlayers: 2 }
    When method POST
    Then status 404

    Given path '/api/matches'
    And request { hostPlayerId: '#(hostId)', maxPlayers: 2 }
    When method POST
    Then status 201
    And match response.id == '#string'
    And match response.status == 'WAITING'
    * def matchId = response.id

    Given path '/api/matches'
    And param status = 'WAITING'
    When method GET
    Then status 200
    And match response == '#array'

    Given path '/api/matches', matchId
    When method GET
    Then status 200
    And match response.id == matchId

    Given path '/api/matches', matchId
    And request {}
    When method PUT
    Then status 400

    Given path '/api/matches', matchId
    And request { maxPlayers: 2 }
    When method PUT
    Then status 200

    Given path '/api/matches', matchId
    And request { maxPlayers: 1 }
    When method PATCH
    Then status 400

    Given path '/api/matches', matchId, 'join'
    And request {}
    When method POST
    Then status 400

    Given path '/api/matches', matchId, 'join'
    And request { playerId: '#(guestId)' }
    When method POST
    Then status 200

    Given path '/api/matches', matchId
    When method GET
    Then status 200
    And match response.status == 'READY'
    And match response.players == '#[2]'

    Given path '/api/matches', matchId, 'start'
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'state'
    When method GET
    Then status 200
    And match response.currentRound == 1

    Given path '/api/matches', matchId, 'rounds'
    When method GET
    Then status 200
    And match response == '#[1]'
    * def roundId = response[0].id

    Given path '/api/matches', matchId, 'rounds', roundId
    When method GET
    Then status 200
    And match response.status == 'INITIALIZED'

    Given path '/api/matches', matchId, 'rounds', roundId, 'roll'
    And request { playerId: 'missing-player' }
    When method POST
    Then status 400

    Given path '/api/matches', matchId, 'rounds', roundId, 'roll'
    And request { playerId: '#(hostId)' }
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'rounds', roundId, 'lock'
    And request { playerId: '#(hostId)', lockedIndexes: [0, 2] }
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'rounds', roundId, 'locked-dice'
    And request { locked: [true, false, true, false, true] }
    When method PATCH
    Then status 200
    And match response.locked[4] == true

    Given path '/api/matches', matchId, 'rounds', roundId
    And request { status: 'ABILITY_PHASE' }
    When method PATCH
    Then status 200
    And match response.status == 'ABILITY_PHASE'

    Given path '/api/matches', matchId, 'replay', 'export'
    When method GET
    Then status 200
    And match response contains matchId

    Given path '/api/matches', matchId, 'rounds', roundId, 'resolve'
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'rounds'
    When method GET
    Then status 200
    And match response == '#[2]'

  Scenario: Match removal endpoints - remove player and delete match
    Given path '/api/players'
    And request { name: 'Remove Host' }
    When method POST
    Then status 201
    * def hostId = response.id

    Given path '/api/players'
    And request { name: 'Remove Guest' }
    When method POST
    Then status 201
    * def guestId = response.id

    Given path '/api/matches'
    And request { hostPlayerId: '#(hostId)', maxPlayers: 3 }
    When method POST
    Then status 201
    * def matchId = response.id

    Given path '/api/matches', matchId, 'join'
    And request { playerId: '#(guestId)' }
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'players', 'missing-player'
    When method DELETE
    Then status 404

    Given path '/api/matches', matchId, 'players', guestId
    When method DELETE
    Then status 204

    Given path '/api/matches', matchId, 'status'
    And request {}
    When method PATCH
    Then status 400

    Given path '/api/matches', matchId, 'status'
    And request { status: 'READY' }
    When method PATCH
    Then status 200
    And match response.status == 'READY'

    Given path '/api/matches', matchId
    When method DELETE
    Then status 204

    Given path '/api/matches', matchId
    When method GET
    Then status 404

  Scenario: Nested player abilities and ability activation
    Given path '/api/players'
    And request { name: 'Nested Ability Player' }
    When method POST
    Then status 201
    * def playerId = response.id

    * def abilityId = 'karate-nested-ability-' + uuid()
    Given path '/api/abilities'
    And request { id: '#(abilityId)', name: 'Nested Ability', cost: 0 }
    When method POST
    Then status 201

    Given path '/api/players', playerId, 'abilities'
    And request {}
    When method POST
    Then status 400

    Given path '/api/players', playerId, 'abilities'
    And request { abilityId: '#(abilityId)' }
    When method POST
    Then status 200
    And match response == '#array'

    Given path '/api/players', playerId, 'abilities'
    When method GET
    Then status 200
    * def matchingPlayerAbilities = karate.jsonPath(response, "$[?(@.id=='" + abilityId + "')]")
    And match matchingPlayerAbilities == '#[1]'

    Given path '/api/players', playerId, 'abilities', abilityId
    When method DELETE
    Then status 200

    Given path '/api/players'
    And request { name: 'Activation Host' }
    When method POST
    Then status 201
    * def hostId = response.id

    Given path '/api/players'
    And request { name: 'Activation Guest' }
    When method POST
    Then status 201
    * def guestId = response.id

    Given path '/api/matches'
    And request { hostPlayerId: '#(hostId)', maxPlayers: 2 }
    When method POST
    Then status 201
    * def matchId = response.id

    Given path '/api/matches', matchId, 'join'
    And request { playerId: '#(guestId)' }
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'start'
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'rounds'
    When method GET
    Then status 200
    * def roundId = response[0].id

    Given path '/api/matches', matchId, 'rounds', roundId, 'abilities', 'activate'
    And request {}
    When method POST
    Then status 400

    Given path '/api/matches', matchId, 'rounds', roundId, 'abilities', 'activate'
    And request { playerId: '#(hostId)', abilityId: '#(abilityId)', targetId: '#(guestId)' }
    When method POST
    Then status 200

    Given path '/api/matches', matchId, 'rounds', roundId
    When method GET
    Then status 200
    And match response.status == 'ABILITY_PHASE'

  Scenario: Files and ability packs - avatar, import, list, replace, patch and delete
    Given path '/api/players'
    And request { name: 'File Player' }
    When method POST
    Then status 201
    * def playerId = response.id

    Given path '/api/players', playerId, 'avatar'
    And multipart file file = { read: 'classpath:com/diceduel/test-avatar.png', filename: 'avatar.png', contentType: 'image/png' }
    When method POST
    Then status 200

    Given path '/api/players', playerId, 'avatar'
    And multipart file file = { read: 'classpath:com/diceduel/not-avatar.txt', filename: 'avatar.txt', contentType: 'text/plain' }
    When method POST
    Then status 400

    Given path '/api/players', playerId, 'avatar'
    And multipart file file = { read: 'classpath:com/diceduel/test-avatar.png', filename: 'avatar.png', contentType: 'image/png' }
    When method PUT
    Then status 200

    Given path '/api/players', playerId, 'avatar'
    When method DELETE
    Then status 204

    Given path '/api/ability-packs'
    When method GET
    Then status 200
    And match response == '#array'

    Given path '/api/ability-packs', 'missing-pack'
    When method GET
    Then status 404

    Given path '/api/ability-packs', 'import'
    And multipart file file = { read: 'classpath:com/diceduel/empty-pack.csv', filename: 'empty-pack.csv', contentType: 'text/plain' }
    When method POST
    Then status 400

    Given path '/api/ability-packs', 'import'
    And multipart file file = { read: 'classpath:com/diceduel/ability-pack.csv', filename: 'ability-pack.csv', contentType: 'text/plain' }
    When method POST
    Then status 201
    And match response.id == '#string'
    * def packId = response.id

    Given path '/api/ability-packs', packId
    When method GET
    Then status 200
    And match response.id == packId

    Given path '/api/ability-packs', packId
    And multipart file file = { read: 'classpath:com/diceduel/ability-pack-replacement.csv', filename: 'ability-pack-replacement.csv', contentType: 'text/plain' }
    And param name = 'Replacement Pack'
    When method PUT
    Then status 200
    And match response.name == 'Replacement Pack'

    Given path '/api/ability-packs', packId
    And request { name: '' }
    When method PATCH
    Then status 400

    Given path '/api/ability-packs', packId
    And request { description: 'Karate metadata' }
    When method PATCH
    Then status 200
    And match response.description == 'Karate metadata'

    Given path '/api/ability-packs', packId
    When method DELETE
    Then status 204

    Given path '/api/ability-packs', packId
    When method GET
    Then status 404
