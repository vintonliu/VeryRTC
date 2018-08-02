/*
  The oRTP library is an RTP (Realtime Transport Protocol - rfc3550) stack.
  Copyright (C) 2001  Simon MORLAT simon.morlat@linphone.org

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


#include "logging.h"
#ifdef ANDROID
#include <android/log.h>
#define LOGGING(lev, ...) ((void)__android_log_print(lev, "SipJni", __VA_ARGS__))
#endif

static FILE *__log_file=0;

/**
 *@param file a FILE pointer where to output the ortp logs.
 *
**/
void ortp_set_log_file(FILE *file)
{
	__log_file=file;
}

static void __ortp_logv_out(OrtpLogLevel lev, const char *fmt, va_list args);

OrtpLogFunc ortp_logv_out=__ortp_logv_out;

/**
 *@param func: your logging function, compatible with the OrtpLogFunc prototype.
 *
**/
void ortp_set_log_handler(OrtpLogFunc func){
	ortp_logv_out=func;
}


static unsigned int __ortp_log_mask=ORTP_WARNING|ORTP_ERROR|ORTP_FATAL;

/**
 * @ param levelmask a mask of ORTP_DEBUG, ORTP_MESSAGE, ORTP_WARNING, ORTP_ERROR
 * ORTP_FATAL .
**/
void ortp_set_log_level_mask(int levelmask){
	__ortp_log_mask=levelmask;
}

int ortp_get_log_level_mask(void) {
	return __ortp_log_mask;
}

char * ortp_strdup_vprintf(const char *fmt, va_list ap)
{
	/* Guess we need no more than 100 bytes. */
	int n, size = 256;
	char *p,*np;
#ifndef WIN32
	va_list cap;/*copy of our argument list: a va_list cannot be re-used (SIGSEGV on linux 64 bits)*/
#endif
	if ((p = (char *) ortp_malloc (size)) == NULL)
		return NULL;
	while (1)
	{
		/* Try to print in the allocated space. */
#ifndef WIN32
		va_copy(cap,ap);
		n = vsnprintf (p, size, fmt, cap);
		va_end(cap);
#else
		/*this works on 32 bits, luckily*/
		n = vsnprintf (p, size, fmt, ap);
#endif
		/* If that worked, return the string. */
		if (n > -1 && n < size)
			return p;
		//printf("Reallocing space.\n");
		/* Else try again with more space. */
		if (n > -1)	/* glibc 2.1 */
			size = n + 1;	/* precisely what is needed */
		else		/* glibc 2.0 */
			size *= 2;	/* twice the old size */
		if ((np = (char *) ortp_realloc (p, size)) == NULL)
		{
		  free(p);
		  return NULL;
		}
		else
		{
		  p = np;
		}
	}
}

char *ortp_strdup_printf(const char *fmt,...){
	char *ret;
	va_list args;
	va_start (args, fmt);
	ret=ortp_strdup_vprintf(fmt, args);
	va_end (args);
	return ret;
}

#if	defined(WIN32) || defined(_WIN32_WCE)
#define ENDLINE "\r\n"
#else
#define ENDLINE "\n"
#endif

#if	defined(WIN32) || defined(_WIN32_WCE)
void ortp_logv(int level, const char *fmt, va_list args)
{
	if (ortp_logv_out!=NULL && ortp_log_level_enabled(level))
		ortp_logv_out(level,fmt,args);
#if !defined(_WIN32_WCE)
	if ((level)==ORTP_FATAL) abort();
#endif
}
#endif

static void __ortp_logv_out(OrtpLogLevel lev, const char *fmt, va_list args){
	const char *lname="undef";
	char *msg = NULL;
#ifdef ANDROID
    int level = ANDROID_LOG_INFO;
#endif

	if (__log_file==NULL) __log_file=stderr;
	switch(lev){
		case ORTP_DEBUG:
			lname="debug";
#ifdef ANDROID
			level = ANDROID_LOG_DEBUG;
#endif
			break;
		case ORTP_MESSAGE:
			lname="message";
#ifdef ANDROID
			level = ANDROID_LOG_INFO;
#endif
			break;
		case ORTP_WARNING:
			lname="warning";
#ifdef ANDROID
			level = ANDROID_LOG_WARN;
#endif
			break;
		case ORTP_ERROR:
			lname="error";
#ifdef ANDROID
			level = ANDROID_LOG_ERROR;
#endif
			break;
		case ORTP_FATAL:
			lname="fatal";
#ifdef ANDROID
			level = ANDROID_LOG_FATAL;
#endif
			break;
		default:
			ortp_fatal("Bad level !");
	}

	msg = ortp_strdup_vprintf(fmt, args);
	if (msg != NULL)
	{
		int len = strlen(msg);
		/* need to remove endline */
		if (len > 1) {
			if (msg[len - 1] == '\n')
				msg[len - 1] = '\0';
			if (msg[len - 2] == '\r')
				msg[len - 2] = '\0';
		}

		len += 128;
		char *tmpBuf = ortp_malloc(len);
		if (tmpBuf == NULL)
		{
			ortp_free(msg);
			msg = NULL;
			return;
		}

		snprintf(tmpBuf, len, "sip-%s-%s"ENDLINE, lname, msg);

#if defined(_MSC_VER) && !defined(_WIN32_WCE)
#ifdef UNICODE
		WCHAR wUnicode[4096 * 2];
		int size;

		size = MultiByteToWideChar(CP_UTF8, 0, tmpBuf, -1, wUnicode, sizeof(wUnicode));
		OutputDebugString(wUnicode);
#else
		OutputDebugString(tmpBuf);
#endif
#endif
#ifdef ANDROID
		LOGGING(level, "%s", tmpBuf);
#endif

		fprintf(__log_file, tmpBuf);
		fflush(__log_file);
		ortp_free(msg);
		msg = NULL;

		ortp_free(tmpBuf);
		tmpBuf = NULL;
	}
}
