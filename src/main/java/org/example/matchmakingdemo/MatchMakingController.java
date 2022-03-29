package org.example.matchmakingdemo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matchmaking")
public class MatchMakingController {

	@PostMapping("start/playerid/{playerId}/rank/{rank}")
	public String testTp(@PathVariable Long playerId, @PathVariable Integer rank) {
		MatchMaker.INSTANCE.putPlayerIntoPool(playerId, rank);
		return "Success";
	}

	@PutMapping("cancel/playerid/{playerId}")
	public String testTp(@PathVariable Long playerId) {
		MatchMaker.INSTANCE.removePlayerFromPool(playerId);
		return "Success";
	}

}
