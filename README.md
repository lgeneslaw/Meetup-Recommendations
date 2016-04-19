# Meetup-Recommendations

## Installation
Simply download GroupFinderByDay.jar and run

`java -jar GroupFinderByDay.jar`

## Background
When I moved to New York, I wanted to find some sort of group/activity to be a part of. I remember having trouble finding something that I would be interested that also met on a regular basis and worked with my schedule. I would have liked to have a service that recommends groups/activities for me and also knows my preferred meeting days/times.

## What it does/How it works
This application asks the user to specify which day of the week they prefer to meet. It then pulls in 100 recommended groups, (in this case it pulls in recommended groups for my account). For each group, it requests 10 upcoming events, then finds which of the groups have at least 1 upcoming event on the preferred day of the week. Each group that matches this criteria is printed on the command line.

## Future Thoughts
This use case would be a proof of concept for a tool that suggests groups based not only on interests, but on preferred meeting patterns as well. I could see something like this being very useful if it asked users for preferred days of the week, times, and meeting frequencies (weekly, monthly, etc.). It could then analyze their recommended groups to find which ones rank highly according to their meeting behavior.
