/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
*****************************************************************************/
package ab.server.proxy.message;

import org.json.simple.JSONObject;

import ab.server.ProxyMessage;

public class ProxyDragMessage implements ProxyMessage<Object> {
	private int x, y, dx, dy;
	
	public ProxyDragMessage(int x, int y, int dx, int dy) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
	}
	
	@Override
	public String getMessageName() {
		return "drag";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		o.put("x", x);
		o.put("y", y);
		o.put("dx", dx);
		o.put("dy", dy);
		return o;
	}
	
	@Override
	public Object gotResponse(JSONObject data) {
		return new Object();
	}
}
