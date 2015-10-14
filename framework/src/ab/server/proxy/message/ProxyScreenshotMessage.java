/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
*****************************************************************************/
package ab.server.proxy.message;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

import ab.server.ProxyMessage;

// request a screenshot from the game
public class ProxyScreenshotMessage implements ProxyMessage<byte[]> {
    @Override
    public String getMessageName() {
        return "screenshot";
    }

    @Override
    public JSONObject getJSON() {
        return new JSONObject();
    }

    @Override
    public byte[] gotResponse(JSONObject data) {
        String imageStr = (String) data.get("data");
        imageStr = imageStr.split(",", 2)[1];
        byte[] imageBytes = Base64.decodeBase64(imageStr);
        return imageBytes;
    }
}
