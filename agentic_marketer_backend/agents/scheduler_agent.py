import random

class SchedulerAgent:
    def get_best_time(self, platform: str) -> str:
        best_times = {
            "instagram": "18:00",  # 6 PM
            "facebook": "13:00",   # 1 PM
            "tiktok": "19:00",     # 7 PM
            "linkedin": "08:30"    # 8:30 AM
        }

        time = best_times.get(platform.lower(), "12:00")
        hour, minute = map(int, time.split(':'))
        hour = min(23, max(0, hour + random.randint(-1, 1)))

        return f"{hour:02d}:{minute:02d}"
