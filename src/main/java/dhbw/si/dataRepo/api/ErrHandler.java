package dhbw.si.dataRepo.api;

import java.net.http.HttpResponse;

/**
 * @author V. Richter
 */
@FunctionalInterface
public interface ErrHandler {
	HttpResponse<String> handle(HttpResponse<String> res, APIReq api) throws APIErr;
}